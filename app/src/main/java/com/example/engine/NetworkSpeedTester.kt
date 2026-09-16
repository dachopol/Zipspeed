package com.example.engine

import com.example.model.ServerInfo
import com.example.model.SpeedTestState
import com.example.model.TestPhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class NetworkSpeedTester {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .build()

    private val _state = MutableStateFlow(SpeedTestState())
    val state: StateFlow<SpeedTestState> = _state.asStateFlow()

    private val activeCalls = CopyOnWriteArrayList<Call>()
    private val isCancelled = AtomicBoolean(false)

    private fun cancelAllActiveCalls() {
        isCancelled.set(true)
        for (call in activeCalls) {
            try {
                call.cancel()
            } catch (_: Exception) {}
        }
        activeCalls.clear()
    }

    suspend fun runSpeedTest(
        server: ServerInfo,
        isPrecisionMode: Boolean = false,
        isPro: Boolean = false,
        batterySaver: Boolean = false
    ): SpeedTestState = supervisorScope {
        isCancelled.set(false)
        cancelAllActiveCalls()

        _state.value = SpeedTestState(
            phase = TestPhase.TESTING_PING,
            liveSpeed = 0.0,
            progressFraction = 0.05f,
            isPrecisionMode = isPrecisionMode
        )

        val pollDelayMs = if (batterySaver) 75L else 45L
        val testStartTime = System.currentTimeMillis()

        // =========================================================================
        // PHASE 1: HTTP LATENCY & JITTER MEASUREMENT (Real HTTP Round-Trip Time)
        // =========================================================================
        val pingCount = if (isPrecisionMode) 8 else 4
        val pingResults = mutableListOf<Long>()
        var detectedColo: String? = null
        var detectedIp: String? = null
        var detectedAsn: String? = null

        val pingUrl = if (server.hostUrl.isNotBlank()) server.hostUrl else "https://speed.cloudflare.com/__down?bytes=0"

        for (i in 1..pingCount) {
            if (isCancelled.get() || !isActive) {
                _state.update { it.copy(phase = TestPhase.CANCELLED, liveSpeed = 0.0) }
                return@supervisorScope _state.value
            }

            val requestStart = System.currentTimeMillis()
            var pingDuration: Long? = null

            withTimeoutOrNull(3500L) {
                try {
                    val request = Request.Builder()
                        .url(pingUrl)
                        .header("User-Agent", "Zipspeed/1.0")
                        .header("Cache-Control", "no-cache")
                        .build()

                    val call = client.newCall(request)
                    activeCalls.add(call)

                    try {
                        withContext(Dispatchers.IO) {
                            call.execute().use { response ->
                                pingDuration = System.currentTimeMillis() - requestStart
                                if (detectedColo == null) {
                                    detectedColo = response.header("cf-meta-colo") ?: response.header("colo")
                                    detectedIp = response.header("cf-meta-ip")
                                    detectedAsn = response.header("asn") ?: response.header("cf-meta-asn")
                                }
                            }
                        }
                    } finally {
                        activeCalls.remove(call)
                    }
                } catch (_: IOException) {
                    pingDuration = null
                }
            }

            if (pingDuration != null && pingDuration!! > 0) {
                pingResults.add(pingDuration!!)
                _state.update {
                    it.copy(
                        pingMs = pingDuration!!.toInt(),
                        progressFraction = 0.05f + (i.toFloat() / pingCount * 0.12f),
                        detectedColo = detectedColo,
                        detectedClientIp = detectedIp,
                        detectedAsn = detectedAsn
                    )
                }
            }

            delay(if (batterySaver) 80L else 40L)
        }

        if (pingResults.isEmpty()) {
            _state.update {
                it.copy(
                    phase = TestPhase.ERROR,
                    errorMessage = "ไม่สามารถเชื่อมต่อเซิร์ฟเวอร์ทดสอบได้ กรุณาตรวจสอบการเชื่อมต่ออินเทอร์เน็ต (Connection timed out or unreachable)",
                    liveSpeed = 0.0
                )
            }
            return@supervisorScope _state.value
        }

        val sortedPings = pingResults.sorted()
        val finalPing = (sortedPings.sum() / sortedPings.size).toInt()
        val jitter = if (pingResults.size > 1) {
            var sumDiff = 0.0
            for (idx in 1 until pingResults.size) {
                sumDiff += abs(pingResults[idx] - pingResults[idx - 1])
            }
            (sumDiff / (pingResults.size - 1)).roundToInt()
        } else 0

        _state.update {
            it.copy(
                pingMs = finalPing,
                jitterMs = jitter,
                phase = TestPhase.TESTING_DOWNLOAD,
                progressFraction = 0.18f
            )
        }

        delay(60)

        // =========================================================================
        // PHASE 2: REAL DOWNLOAD MEASUREMENT (Stream bytes from Cloudflare Edge)
        // Formula: Mbps = (DownloadedBytes * 8) / (ElapsedSeconds * 1,000,000)
        // =========================================================================
        val downloadDurationMs = if (isPrecisionMode) 7000L else if (batterySaver) 3200L else 4500L
        val bytesPerChunk = if (isPrecisionMode) 35000000L else 20000000L
        val downloadUrl = "${server.downloadUrl}?bytes=$bytesPerChunk"
        val totalDownloadedBytes = AtomicLong(0L)
        val isDownloadingActive = AtomicBoolean(true)
        val numDownloadThreads = if (isPro) 3 else 2

        val downloadWorkers = (1..numDownloadThreads).map {
            launch(Dispatchers.IO) {
                val buffer = ByteArray(32768)
                while (isDownloadingActive.get() && !isCancelled.get() && isActive) {
                    try {
                        val request = Request.Builder()
                            .url(downloadUrl)
                            .header("User-Agent", "Zipspeed/1.0")
                            .header("Cache-Control", "no-cache")
                            .build()

                        val call = client.newCall(request)
                        activeCalls.add(call)
                        try {
                            call.execute().use { response ->
                                val stream = response.body?.byteStream()
                                if (stream != null) {
                                    var readBytes = 0
                                    while (isDownloadingActive.get() && !isCancelled.get() && isActive &&
                                        stream.read(buffer).also { readBytes = it } != -1
                                    ) {
                                        totalDownloadedBytes.addAndGet(readBytes.toLong())
                                    }
                                }
                            }
                        } finally {
                            activeCalls.remove(call)
                        }
                    } catch (_: Exception) {
                        delay(60L)
                    }
                }
            }
        }

        val dlStartTime = System.currentTimeMillis()
        var lastDlBytes = 0L
        var lastDlTime = dlStartTime
        val recordedDlSamples = mutableListOf<Double>()
        var smoothedDlSpeed = 0.0

        while (isActive && !isCancelled.get() && (System.currentTimeMillis() - dlStartTime) < downloadDurationMs) {
            delay(pollDelayMs)
            val now = System.currentTimeMillis()
            val currentBytes = totalDownloadedBytes.get()
            val deltaBytes = currentBytes - lastDlBytes
            val timeDiffSec = (now - lastDlTime) / 1000.0
            val totalElapsedSec = (now - dlStartTime) / 1000.0

            if (timeDiffSec >= 0.04) {
                val instantMbps = if (deltaBytes > 0 && timeDiffSec > 0) {
                    (deltaBytes * 8.0) / (timeDiffSec * 1_000_000.0)
                } else 0.0

                if (instantMbps > 0.0) {
                    smoothedDlSpeed = if (smoothedDlSpeed == 0.0) instantMbps else (0.45 * instantMbps + 0.55 * smoothedDlSpeed)
                    if (totalElapsedSec > 0.3) {
                        recordedDlSamples.add(smoothedDlSpeed)
                    }
                }

                val progress = (totalElapsedSec / (downloadDurationMs / 1000.0)).toFloat().coerceIn(0f, 1f)
                _state.update {
                    it.copy(
                        liveSpeed = smoothedDlSpeed,
                        progressFraction = 0.18f + (progress * 0.38f),
                        downloadSamples = recordedDlSamples.takeLast(30)
                    )
                }
                lastDlBytes = currentBytes
                lastDlTime = now
            }
        }

        isDownloadingActive.set(false)
        downloadWorkers.forEach { it.cancel() }
        cancelAllActiveCalls()
        isCancelled.set(false)

        val dlElapsedSec = (System.currentTimeMillis() - dlStartTime) / 1000.0
        val finalDlBytes = totalDownloadedBytes.get()

        if (finalDlBytes == 0L || dlElapsedSec <= 0) {
            _state.update {
                it.copy(
                    phase = TestPhase.ERROR,
                    errorMessage = "การทดสอบดาวน์โหลดล้มเหลว ไม่สามารถรับข้อมูลจากเซิร์ฟเวอร์ได้ (Download failed: No bytes received)",
                    liveSpeed = 0.0
                )
            }
            return@supervisorScope _state.value
        }

        // Real Download Calculation: (TotalBytes * 8) / (ElapsedSeconds * 1,000,000)
        // Or 80th percentile of recorded samples during stable streaming
        val finalDownloadMbps = if (recordedDlSamples.isNotEmpty()) {
            val sorted = recordedDlSamples.sorted()
            val idx = (sorted.size * 0.75).toInt().coerceIn(0, sorted.size - 1)
            sorted[idx]
        } else {
            (finalDlBytes * 8.0) / (dlElapsedSec * 1_000_000.0)
        }

        _state.update {
            it.copy(
                downloadMbps = finalDownloadMbps,
                bytesDownloaded = finalDlBytes,
                phase = TestPhase.TESTING_UPLOAD,
                progressFraction = 0.58f,
                liveSpeed = 0.0
            )
        }

        delay(80)

        // =========================================================================
        // PHASE 3: REAL UPLOAD MEASUREMENT (POST real bytes to Cloudflare Edge)
        // Formula: Mbps = (UploadedBytes * 8) / (ElapsedSeconds * 1,000,000)
        // =========================================================================
        val uploadDurationMs = if (isPrecisionMode) 6000L else if (batterySaver) 2800L else 3800L
        val uploadUrl = server.uploadUrl
        val totalUploadedBytes = AtomicLong(0L)
        val isUploadingActive = AtomicBoolean(true)
        val ulStartTime = System.currentTimeMillis()

        val uploadWorker = launch(Dispatchers.IO) {
            val uploadChunk = ByteArray(32768) { (it % 127).toByte() }
            while (isUploadingActive.get() && !isCancelled.get() && isActive) {
                try {
                    val requestBody = object : RequestBody() {
                        override fun contentType() = "application/octet-stream".toMediaTypeOrNull()
                        override fun contentLength() = -1L
                        override fun writeTo(sink: BufferedSink) {
                            while (isUploadingActive.get() && !isCancelled.get() && isActive &&
                                (System.currentTimeMillis() - ulStartTime) < uploadDurationMs
                            ) {
                                sink.write(uploadChunk)
                                totalUploadedBytes.addAndGet(uploadChunk.size.toLong())
                            }
                            sink.flush()
                        }
                    }

                    val request = Request.Builder()
                        .url(uploadUrl)
                        .header("User-Agent", "Zipspeed/1.0")
                        .post(requestBody)
                        .build()

                    val call = client.newCall(request)
                    activeCalls.add(call)
                    try {
                        call.execute().close()
                    } finally {
                        activeCalls.remove(call)
                    }
                } catch (_: Exception) {
                    delay(50L)
                }
            }
        }

        var lastUlBytes = 0L
        var lastUlTime = ulStartTime
        val recordedUlSamples = mutableListOf<Double>()
        var smoothedUlSpeed = 0.0

        while (isActive && !isCancelled.get() && (System.currentTimeMillis() - ulStartTime) < uploadDurationMs) {
            delay(pollDelayMs)
            val now = System.currentTimeMillis()
            val currentBytes = totalUploadedBytes.get()
            val deltaBytes = currentBytes - lastUlBytes
            val timeDiffSec = (now - lastUlTime) / 1000.0
            val totalElapsedSec = (now - ulStartTime) / 1000.0

            if (timeDiffSec >= 0.04) {
                val instantMbps = if (deltaBytes > 0 && timeDiffSec > 0) {
                    (deltaBytes * 8.0) / (timeDiffSec * 1_000_000.0)
                } else 0.0

                if (instantMbps > 0.0) {
                    smoothedUlSpeed = if (smoothedUlSpeed == 0.0) instantMbps else (0.45 * instantMbps + 0.55 * smoothedUlSpeed)
                    if (totalElapsedSec > 0.3) {
                        recordedUlSamples.add(smoothedUlSpeed)
                    }
                }

                val progress = (totalElapsedSec / (uploadDurationMs / 1000.0)).toFloat().coerceIn(0f, 1f)
                _state.update {
                    it.copy(
                        liveSpeed = smoothedUlSpeed,
                        progressFraction = 0.58f + (progress * 0.40f),
                        uploadSamples = recordedUlSamples.takeLast(30)
                    )
                }
                lastUlBytes = currentBytes
                lastUlTime = now
            }
        }

        isUploadingActive.set(false)
        uploadWorker.cancel()
        cancelAllActiveCalls()

        val ulElapsedSec = (System.currentTimeMillis() - ulStartTime) / 1000.0
        val finalUlBytes = totalUploadedBytes.get()

        val finalUploadMbps = if (recordedUlSamples.isNotEmpty()) {
            val sorted = recordedUlSamples.sorted()
            val idx = (sorted.size * 0.75).toInt().coerceIn(0, sorted.size - 1)
            sorted[idx]
        } else if (finalUlBytes > 0 && ulElapsedSec > 0) {
            (finalUlBytes * 8.0) / (ulElapsedSec * 1_000_000.0)
        } else {
            0.0
        }

        // Warnings Check (Latency > 150ms or Jitter > 30ms)
        val warning = when {
            finalPing > 150 -> "ค่า HTTP Latency สูงผิดปกติ (${finalPing}ms) อาจส่งผลต่อการเล่นเกมหรือการสนทนาเสียง"
            jitter > 35 -> "พบความผันผวนของสัญญาณ (Jitter ${jitter}ms) แนะนำให้อยู่ใกล้เราเตอร์หรือจุดปล่อยสัญญาณ"
            else -> null
        }

        _state.update {
            it.copy(
                uploadMbps = finalUploadMbps,
                bytesUploaded = finalUlBytes,
                liveSpeed = finalDownloadMbps,
                progressFraction = 1.0f,
                phase = TestPhase.COMPLETED,
                warningMessage = warning,
                testDurationMs = System.currentTimeMillis() - testStartTime
            )
        }

        _state.value
    }

    fun cancelTest() {
        cancelAllActiveCalls()
        _state.update {
            it.copy(
                phase = TestPhase.CANCELLED,
                liveSpeed = 0.0,
                progressFraction = 0.0f
            )
        }
    }

    fun reset() {
        cancelAllActiveCalls()
        _state.value = SpeedTestState(phase = TestPhase.IDLE, liveSpeed = 0.0)
    }
}
