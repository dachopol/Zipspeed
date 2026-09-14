package com.example.engine

import com.example.model.ServerInfo
import com.example.model.SpeedTestState
import com.example.model.TestPhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
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

    suspend fun runSpeedTest(
        server: ServerInfo,
        isPro: Boolean,
        batterySaver: Boolean = false
    ): SpeedTestState = coroutineScope {
        _state.value = SpeedTestState(phase = TestPhase.TESTING_PING, liveSpeed = 0.0)

        val pollDelayMs = if (batterySaver) 100L else 50L
        val pingDelayMs = if (batterySaver) 180L else 90L

        // 1. PING & JITTER PHASE (Real HTTP Round-Trip Time with Warm Connection)
        val pingResults = mutableListOf<Long>()
        val pingUrl = if (server.hostUrl.startsWith("http")) server.hostUrl else "https://${server.hostUrl}"
        var failedPings = 0

        // Warm up socket and TLS session so handshake doesn't falsely inflate latency
        try {
            withContext(Dispatchers.IO) {
                val warmReq = Request.Builder()
                    .url(pingUrl)
                    .header("User-Agent", "Zipspeed/35.0")
                    .head()
                    .build()
                client.newCall(warmReq).execute().close()
            }
        } catch (_: Exception) {}

        for (i in 1..4) {
            if (!isActive) break
            val startTime = System.currentTimeMillis()
            var measuredPing: Long = -1L
            try {
                withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url(pingUrl)
                        .header("User-Agent", "Zipspeed/35.0")
                        .head()
                        .build()
                    client.newCall(request).execute().use {
                        measuredPing = System.currentTimeMillis() - startTime
                    }
                }
            } catch (e: Exception) {
                // If ping to host fails, try Cloudflare Anycast ping endpoint
                try {
                    val fallbackStart = System.currentTimeMillis()
                    withContext(Dispatchers.IO) {
                        val fbRequest = Request.Builder()
                            .url("https://speed.cloudflare.com/__down?bytes=0")
                            .header("User-Agent", "Zipspeed/35.0")
                            .head()
                            .build()
                        client.newCall(fbRequest).execute().use {
                            measuredPing = System.currentTimeMillis() - fallbackStart
                        }
                    }
                } catch (e2: Exception) {
                    failedPings++
                }
            }

            if (measuredPing > 0) {
                pingResults.add(measuredPing)
                _state.update {
                    it.copy(
                        pingMs = measuredPing.toInt(),
                        progressFraction = 0.05f + (i * 0.03f)
                    )
                }
            }
            delay(pingDelayMs)
        }

        // If all ping probes failed, device is offline or has no internet connection
        if (pingResults.isEmpty()) {
            _state.update {
                it.copy(
                    phase = TestPhase.ERROR,
                    errorMessage = "ไม่พบการเชื่อมต่ออินเทอร์เน็ต กรุณาเปิด Wi-Fi หรือข้อมูลมือถือ",
                    liveSpeed = 0.0,
                    progressFraction = 0.0f
                )
            }
            return@coroutineScope _state.value
        }

        // Use median ping or minimum for true network RTT (standard in Ookla & M-Lab)
        val sorted = pingResults.sorted()
        val finalPing = sorted[sorted.size / 2].toInt().coerceAtLeast(10)

        val jitter = if (pingResults.size > 1) {
            var sumDiff = 0.0
            for (i in 1 until pingResults.size) {
                sumDiff += abs(pingResults[i] - pingResults[i - 1])
            }
            (sumDiff / (pingResults.size - 1)).roundToInt().coerceAtLeast(1)
        } else 2

        val packetLossPercent = (failedPings.toDouble() / 4.0) * 100.0

        _state.update {
            it.copy(
                pingMs = finalPing,
                jitterMs = max(1, jitter),
                packetLossPercent = packetLossPercent,
                phase = TestPhase.TESTING_DOWNLOAD
            )
        }

        delay(150)

        // 2. REAL DOWNLOAD THROUGHPUT MEASUREMENT
        // Connects to high-performance Anycast CDN endpoint and reads real bytes
        val downloadDurationMs = if (batterySaver) 4500L else 6000L
        val downloadUrl = "https://speed.cloudflare.com/__down?bytes=50000000"
        val totalDownloadedBytes = AtomicLong(0L)
        val isDownloadingActive = AtomicBoolean(true)
        val numThreads = if (isPro) 4 else 2

        val downloadWorkers = (1..numThreads).map {
            launch(Dispatchers.IO) {
                val buffer = ByteArray(32768)
                while (isDownloadingActive.get() && isActive) {
                    try {
                        val request = Request.Builder()
                            .url(downloadUrl)
                            .header("User-Agent", "Zipspeed/35.0")
                            .header("Cache-Control", "no-cache")
                            .build()
                        client.newCall(request).execute().use { response ->
                            val stream = response.body?.byteStream()
                            if (stream != null) {
                                var bytesRead = 0
                                while (isDownloadingActive.get() && isActive && stream.read(buffer).also { bytesRead = it } != -1) {
                                    totalDownloadedBytes.addAndGet(bytesRead.toLong())
                                }
                            }
                        }
                    } catch (e: Exception) {
                        delay(100)
                    }
                }
            }
        }

        // Live speed monitoring loop
        val downloadStartTime = System.currentTimeMillis()
        var lastBytes = 0L
        var lastTime = downloadStartTime
        val recordedSpeedSamples = mutableListOf<Double>()
        var smoothedSpeed = 0.0

        while (isActive && System.currentTimeMillis() - downloadStartTime < downloadDurationMs) {
            delay(pollDelayMs)
            val now = System.currentTimeMillis()
            val currentBytes = totalDownloadedBytes.get()
            val timeDiffSec = (now - lastTime) / 1000.0
            val elapsedTotalSec = (now - downloadStartTime) / 1000.0

            if (timeDiffSec >= 0.08) {
                val deltaBytes = currentBytes - lastBytes
                val instantMbps = (deltaBytes * 8.0) / (timeDiffSec * 1_000_000.0)
                val validInstantMbps = max(0.0, instantMbps)

                smoothedSpeed = if (smoothedSpeed == 0.0) validInstantMbps else (0.65 * validInstantMbps + 0.35 * smoothedSpeed)
                if (elapsedTotalSec > 0.8) {
                    recordedSpeedSamples.add(smoothedSpeed)
                }

                val progress = (elapsedTotalSec / (downloadDurationMs / 1000.0)).toFloat().coerceIn(0f, 1f)
                _state.update {
                    it.copy(
                        liveSpeed = smoothedSpeed,
                        progressFraction = 0.17f + (progress * 0.40f)
                    )
                }
                lastBytes = currentBytes
                lastTime = now
            }
        }

        isDownloadingActive.set(false)
        downloadWorkers.forEach { it.cancel() }

        val downloadedBytesCount = totalDownloadedBytes.get()
        if (downloadedBytesCount <= 0L) {
            _state.update {
                it.copy(
                    phase = TestPhase.ERROR,
                    errorMessage = "การเชื่อมต่อขัดข้อง ไม่สามารถดาวน์โหลดข้อมูลจากเซิร์ฟเวอร์ได้",
                    liveSpeed = 0.0
                )
            }
            return@coroutineScope _state.value
        }

        val totalDownloadElapsedSec = max(1.0, (System.currentTimeMillis() - downloadStartTime) / 1000.0)
        val overallAverageDownload = (downloadedBytesCount * 8.0) / (totalDownloadElapsedSec * 1_000_000.0)
        val finalDownload = if (recordedSpeedSamples.size >= 5) {
            val sorted = recordedSpeedSamples.sorted()
            val p80Index = (sorted.size * 0.80).toInt().coerceIn(0, sorted.size - 1)
            sorted[p80Index]
        } else {
            overallAverageDownload
        }

        _state.update {
            it.copy(
                downloadMbps = finalDownload,
                phase = TestPhase.TESTING_UPLOAD,
                liveSpeed = 0.0
            )
        }

        delay(250)

        // 3. REAL UPLOAD THROUGHPUT MEASUREMENT
        // Streams real HTTP POST bytes to Cloudflare Anycast upload endpoint
        val uploadDurationMs = if (batterySaver) 4000L else 5000L
        val uploadUrl = "https://speed.cloudflare.com/__up"
        val totalUploadedBytes = AtomicLong(0L)
        val isUploadingActive = AtomicBoolean(true)
        val uploadStartTime = System.currentTimeMillis()

        val uploadWorker = launch(Dispatchers.IO) {
            val chunk = ByteArray(16384)
            while (isUploadingActive.get() && isActive) {
                try {
                    val requestBody = object : RequestBody() {
                        override fun contentType() = "application/octet-stream".toMediaTypeOrNull()
                        override fun contentLength() = -1L
                        override fun writeTo(sink: BufferedSink) {
                            while (isUploadingActive.get() && isActive && System.currentTimeMillis() - uploadStartTime < uploadDurationMs) {
                                sink.write(chunk)
                                sink.flush()
                                totalUploadedBytes.addAndGet(chunk.size.toLong())
                            }
                        }
                    }

                    val request = Request.Builder()
                        .url(uploadUrl)
                        .header("User-Agent", "Zipspeed/35.0")
                        .post(requestBody)
                        .build()

                    client.newCall(request).execute().close()
                } catch (e: Exception) {
                    delay(100)
                }
            }
        }

        var lastUploadBytes = 0L
        var lastUploadTime = uploadStartTime
        val recordedUploadSamples = mutableListOf<Double>()
        var smoothedUploadSpeed = 0.0

        while (isActive && System.currentTimeMillis() - uploadStartTime < uploadDurationMs) {
            delay(pollDelayMs)
            val now = System.currentTimeMillis()
            val currentBytes = totalUploadedBytes.get()
            val timeDiffSec = (now - lastUploadTime) / 1000.0
            val elapsedTotalSec = (now - uploadStartTime) / 1000.0

            if (timeDiffSec >= 0.08) {
                val deltaBytes = currentBytes - lastUploadBytes
                val instantMbps = (deltaBytes * 8.0) / (timeDiffSec * 1_000_000.0)
                val validInstantMbps = max(0.0, instantMbps)

                smoothedUploadSpeed = if (smoothedUploadSpeed == 0.0) validInstantMbps else (0.65 * validInstantMbps + 0.35 * smoothedUploadSpeed)
                if (elapsedTotalSec > 0.8) {
                    recordedUploadSamples.add(smoothedUploadSpeed)
                }

                val progress = (elapsedTotalSec / (uploadDurationMs / 1000.0)).toFloat().coerceIn(0f, 1f)
                _state.update {
                    it.copy(
                        liveSpeed = smoothedUploadSpeed,
                        progressFraction = 0.57f + (progress * 0.40f)
                    )
                }
                lastUploadBytes = currentBytes
                lastUploadTime = now
            }
        }

        isUploadingActive.set(false)
        uploadWorker.cancel()

        val uploadedBytesCount = totalUploadedBytes.get()
        val totalUploadElapsedSec = max(1.0, (System.currentTimeMillis() - uploadStartTime) / 1000.0)
        val overallAverageUpload = (uploadedBytesCount * 8.0) / (totalUploadElapsedSec * 1_000_000.0)
        val finalUpload = if (recordedUploadSamples.size >= 5) {
            val sorted = recordedUploadSamples.sorted()
            val p80Index = (sorted.size * 0.80).toInt().coerceIn(0, sorted.size - 1)
            sorted[p80Index]
        } else {
            overallAverageUpload
        }

        _state.update {
            it.copy(
                uploadMbps = finalUpload,
                liveSpeed = finalDownload,
                progressFraction = 1.0f,
                phase = TestPhase.COMPLETED
            )
        }

        _state.value
    }

    fun reset() {
        _state.value = SpeedTestState()
    }
}
