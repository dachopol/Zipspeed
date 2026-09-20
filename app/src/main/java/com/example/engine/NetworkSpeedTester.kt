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
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs
import kotlin.math.roundToInt

class NetworkSpeedTester {

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
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
        cancelAllActiveCalls()
        isCancelled.set(false)

        _state.value = SpeedTestState(
            phase = TestPhase.TESTING_PING,
            liveSpeed = 0.0,
            progressFraction = 0.02f,
            isPrecisionMode = isPrecisionMode,
            currentStepText = "สเต็ป 1/3: ตรวจสอบโหนดและวัดค่า Latency..."
        )

        val pollDelayMs = if (batterySaver) 70L else 40L
        val testStartTime = System.currentTimeMillis()

        // =========================================================================
        // สเต็ป 1: วัดค่า LATENCY & JITTER จริงไปยังเซิร์ฟเวอร์ Edge Node
        // =========================================================================
        val pingCount = if (isPrecisionMode) 6 else 4
        val pingResults = mutableListOf<Long>()
        var detectedColo: String? = null
        var detectedIp: String? = null
        var detectedAsn: String? = null

        val pingCandidates = listOfNotNull(
            if (server.hostUrl.isNotBlank()) server.hostUrl else null,
            "https://speed.cloudflare.com/__down?bytes=0",
            "https://cloudflare.com/cdn-cgi/trace",
            "https://1.1.1.1"
        ).distinct()

        for (i in 1..pingCount) {
            if (isCancelled.get() || !isActive) {
                _state.update { it.copy(phase = TestPhase.CANCELLED, liveSpeed = 0.0) }
                return@supervisorScope _state.value
            }

            val candidateUrl = pingCandidates[(i - 1) % pingCandidates.size]
            val requestStart = System.currentTimeMillis()
            var pingDuration: Long? = null

            // แสดงสถานะสเต็ป 1 ชัดเจนโดยเข็มไมล์อยู่ที่ 0.0 (ตรงตามจริง ไม่สร้างความเร็วปลอมช่วง Ping)
            _state.update {
                it.copy(
                    liveSpeed = 0.0,
                    progressFraction = 0.02f + (i.toFloat() / pingCount * 0.12f),
                    currentStepText = "สเต็ป 1/3: วัดค่า Latency ($i/$pingCount) ไปยัง ${server.name}..."
                )
            }

            withTimeoutOrNull(2500L) {
                try {
                    val request = Request.Builder()
                        .url(candidateUrl)
                        .header("User-Agent", "Zipspeed/1.0")
                        .header("Cache-Control", "no-cache")
                        .build()

                    val call = client.newCall(request)
                    activeCalls.add(call)

                    try {
                        withContext(Dispatchers.IO) {
                            call.execute().use { response ->
                                if (response.isSuccessful || response.code in 300..399) {
                                    pingDuration = (System.currentTimeMillis() - requestStart).coerceAtLeast(1L)
                                    if (detectedColo == null) {
                                        detectedColo = response.header("cf-meta-colo") ?: response.header("colo")
                                        detectedIp = response.header("cf-meta-ip")
                                        detectedAsn = response.header("asn") ?: response.header("cf-meta-asn")
                                    }
                                }
                            }
                        }
                    } finally {
                        activeCalls.remove(call)
                    }
                } catch (_: Throwable) {
                    pingDuration = null
                }
            }

            if (pingDuration != null && pingDuration!! > 0) {
                pingResults.add(pingDuration!!)
                _state.update {
                    it.copy(
                        pingMs = pingDuration!!.toInt(),
                        detectedColo = detectedColo,
                        detectedClientIp = detectedIp,
                        detectedAsn = detectedAsn
                    )
                }
            }

            delay(if (batterySaver) 60L else 30L)
        }

        // Real-data rule: never fabricate latency when the endpoint is unreachable.
        if (pingResults.size < 2) {
            _state.update {
                it.copy(
                    phase = TestPhase.ERROR,
                    liveSpeed = 0.0,
                    errorMessage = "ไม่สามารถวัด Latency จริงจากเซิร์ฟเวอร์ได้ กรุณาตรวจสอบอินเทอร์เน็ตแล้วลองใหม่"
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
        } else 2

        _state.update {
            it.copy(
                pingMs = finalPing,
                jitterMs = jitter,
                phase = TestPhase.TESTING_DOWNLOAD,
                progressFraction = 0.15f,
                liveSpeed = 0.0,
                currentStepText = "สเต็ป 2/3: เริ่มต้นระบบดาวน์โหลดแบบเป็นสเต็ป..."
            )
        }

        delay(80)

        // =========================================================================
        // สเต็ป 2: ระบบโหลดเป็นสเต็ป ตรงตามความจริงอ้างอิงจากเซิร์ฟเวอร์ (DOWNLOAD)
        // สเต็ป 2A: อุ่นเครื่องโหนด (Warm-up 2MB)
        // สเต็ป 2B: ทดสอบโหลดสปีดต่อเนื่อง (Ramp-up 8MB)
        // สเต็ป 2C: ทดสอบแบนด์วิดท์สูงสุดตามจริง (Sustained Peak 20MB-35MB)
        // =========================================================================
        val totalDownloadedBytes = AtomicLong(0L)
        val recordedDlSamples = mutableListOf<Double>()
        var smoothedDlSpeed = 0.0

        val downloadSteps = listOf(
            Triple(2_000_000L, 1200L, "สเต็ป 2/3: ดาวน์โหลดสเต็ป 1 (อุ่นเครื่องโหนด 2 MB)..."),
            Triple(8_000_000L, 1600L, "สเต็ป 2/3: ดาวน์โหลดสเต็ป 2 (วัดความเร็วโหลดต่อเนื่อง 8 MB)..."),
            Triple(if (isPrecisionMode) 35_000_000L else 20_000_000L, if (isPrecisionMode) 3500L else 2200L, "สเต็ป 2/3: ดาวน์โหลดสเต็ป 3 (วัดแบนด์วิดท์สูงสุดตามจริง)...")
        )

        val dlOverallStartTime = System.currentTimeMillis()
        var stepProgressBase = 0.15f
        val stepProgressRange = 0.43f / downloadSteps.size

        for ((chunkBytes, stepDurationMs, stepLabel) in downloadSteps) {
            if (isCancelled.get() || !isActive) {
                _state.update { it.copy(phase = TestPhase.CANCELLED, liveSpeed = 0.0) }
                return@supervisorScope _state.value
            }

            _state.update {
                it.copy(
                    currentStepText = stepLabel,
                    progressFraction = stepProgressBase
                )
            }

            val stepStartTime = System.currentTimeMillis()
            var stepLastBytes = totalDownloadedBytes.get()
            var stepLastTime = stepStartTime
            val isStepActive = AtomicBoolean(true)
            val numThreads = if (isPro) 3 else 2

            val stepWorkers = (1..numThreads).map {
                launch(Dispatchers.IO) {
                    val buffer = ByteArray(32768)
                    val url = "${server.downloadUrl}?bytes=$chunkBytes"
                    while (isStepActive.get() && !isCancelled.get() && isActive) {
                        try {
                            val req = Request.Builder()
                                .url(url)
                                .header("User-Agent", "Zipspeed/1.0")
                                .header("Cache-Control", "no-cache")
                                .build()

                            val call = client.newCall(req)
                            activeCalls.add(call)
                            try {
                                call.execute().use { response ->
                                    val stream = if (response.isSuccessful) response.body?.byteStream() else null
                                    if (stream != null) {
                                        var read = 0
                                        while (isStepActive.get() && !isCancelled.get() && isActive &&
                                            stream.read(buffer).also { read = it } != -1
                                        ) {
                                            totalDownloadedBytes.addAndGet(read.toLong())
                                        }
                                    }
                                }
                            } finally {
                                activeCalls.remove(call)
                            }
                        } catch (_: Exception) {
                            delay(40L)
                        }
                    }
                }
            }

            while (isActive && !isCancelled.get() && (System.currentTimeMillis() - stepStartTime) < stepDurationMs) {
                delay(pollDelayMs)
                val now = System.currentTimeMillis()
                val currentBytes = totalDownloadedBytes.get()
                val deltaBytes = currentBytes - stepLastBytes
                val timeDiffSec = (now - stepLastTime) / 1000.0

                if (timeDiffSec >= 0.04) {
                    val instantMbps = if (deltaBytes > 0 && timeDiffSec > 0) {
                        (deltaBytes * 8.0) / (timeDiffSec * 1_000_000.0)
                    } else 0.0

                    if (instantMbps > 0.0) {
                        smoothedDlSpeed = if (smoothedDlSpeed == 0.0) instantMbps else (0.40 * instantMbps + 0.60 * smoothedDlSpeed)
                        recordedDlSamples.add(smoothedDlSpeed)
                    }

                    val stepFrac = ((now - stepStartTime).toFloat() / stepDurationMs).coerceIn(0f, 1f)
                    _state.update {
                        it.copy(
                            liveSpeed = smoothedDlSpeed,
                            progressFraction = stepProgressBase + (stepFrac * stepProgressRange),
                            downloadSamples = recordedDlSamples.takeLast(30)
                        )
                    }
                    stepLastBytes = currentBytes
                    stepLastTime = now
                }
            }

            isStepActive.set(false)
            stepWorkers.forEach { it.cancel() }
            cancelAllActiveCalls()
            isCancelled.set(false)
            stepProgressBase += stepProgressRange
        }

        val dlTotalElapsedSec = ((System.currentTimeMillis() - dlOverallStartTime) / 1000.0).coerceAtLeast(0.001)
        val finalDlBytes = totalDownloadedBytes.get()

        // Average throughput from bytes actually received over the real elapsed interval.
        val finalDownloadMbps = if (finalDlBytes > 0L) {
            (finalDlBytes * 8.0) / (dlTotalElapsedSec * 1_000_000.0)
        } else {
            0.0
        }

        if (finalDownloadMbps == 0.0 && finalDlBytes == 0L) {
            _state.update {
                it.copy(
                    phase = TestPhase.ERROR,
                    errorMessage = "ไม่สามารถเชื่อมต่อเซิร์ฟเวอร์ทดสอบได้ กรุณาตรวจสอบอินเทอร์เน็ต"
                )
            }
            return@supervisorScope _state.value
        }

        _state.update {
            it.copy(
                downloadMbps = finalDownloadMbps,
                bytesDownloaded = finalDlBytes,
                phase = TestPhase.TESTING_UPLOAD,
                progressFraction = 0.58f,
                liveSpeed = 0.0,
                currentStepText = "สเต็ป 3/3: เริ่มต้นระบบอัปโหลดแบบเป็นสเต็ป..."
            )
        }

        delay(80)

        // =========================================================================
        // สเต็ป 3: ระบบอัปโหลดเป็นสเต็ป ตรงตามความจริงอ้างอิงจากเซิร์ฟเวอร์ (UPLOAD)
        // สเต็ป 3A: ทดสอบการส่งข้อมูลเริ่มต้น (Upload Handshake 1MB)
        // สเต็ป 3B: ทดสอบส่งข้อมูลต่อเนื่องตามจริง (Upload Sustained Stream 5MB-10MB)
        // =========================================================================
        val totalUploadedBytes = AtomicLong(0L)
        val recordedUlSamples = mutableListOf<Double>()
        var smoothedUlSpeed = 0.0
        val uploadSteps = listOf(
            Pair(1500L, "สเต็ป 3/3: อัปโหลดสเต็ป 1 (ทดสอบการส่งข้อมูลไปยังโหนด)..."),
            Pair(if (isPrecisionMode) 4500L else 2800L, "สเต็ป 3/3: อัปโหลดสเต็ป 2 (วัดความเร็วอัปโหลดตามจริง)...")
        )

        val ulOverallStartTime = System.currentTimeMillis()
        var ulProgressBase = 0.58f
        val ulProgressRange = 0.40f / uploadSteps.size

        for ((stepDurationMs, stepLabel) in uploadSteps) {
            if (isCancelled.get() || !isActive) {
                _state.update { it.copy(phase = TestPhase.CANCELLED, liveSpeed = 0.0) }
                return@supervisorScope _state.value
            }

            _state.update {
                it.copy(
                    currentStepText = stepLabel,
                    progressFraction = ulProgressBase
                )
            }

            val stepStartTime = System.currentTimeMillis()
            var stepLastBytes = totalUploadedBytes.get()
            var stepLastTime = stepStartTime
            val isStepActive = AtomicBoolean(true)

            val uploadWorker = launch(Dispatchers.IO) {
                val payloadSize = if (isPrecisionMode) 1_000_000 else 512_000
                val uploadPayload = ByteArray(payloadSize) { (it % 127).toByte() }

                while (
                    isStepActive.get() &&
                    !isCancelled.get() &&
                    isActive &&
                    (System.currentTimeMillis() - stepStartTime) < stepDurationMs
                ) {
                    try {
                        val requestBody = object : RequestBody() {
                            override fun contentType() = "application/octet-stream".toMediaTypeOrNull()
                            override fun contentLength() = uploadPayload.size.toLong()
                            override fun writeTo(sink: BufferedSink) {
                                sink.write(uploadPayload)
                                sink.flush()
                            }
                        }

                        val request = Request.Builder()
                            .url(server.uploadUrl)
                            .header("User-Agent", "Zipspeed/1.0")
                            .post(requestBody)
                            .build()

                        val call = client.newCall(request)
                        activeCalls.add(call)
                        try {
                            call.execute().use { response ->
                                if (response.isSuccessful) {
                                    totalUploadedBytes.addAndGet(uploadPayload.size.toLong())
                                }
                            }
                        } finally {
                            activeCalls.remove(call)
                        }
                    } catch (_: Exception) {
                        delay(40L)
                    }
                }
            }

            while (isActive && !isCancelled.get() && (System.currentTimeMillis() - stepStartTime) < stepDurationMs) {
                delay(pollDelayMs)
                val now = System.currentTimeMillis()
                val currentBytes = totalUploadedBytes.get()
                val deltaBytes = currentBytes - stepLastBytes
                val timeDiffSec = (now - stepLastTime) / 1000.0

                if (timeDiffSec >= 0.04) {
                    val instantMbps = if (deltaBytes > 0 && timeDiffSec > 0) {
                        (deltaBytes * 8.0) / (timeDiffSec * 1_000_000.0)
                    } else 0.0

                    if (instantMbps > 0.0) {
                        smoothedUlSpeed = if (smoothedUlSpeed == 0.0) instantMbps else (0.40 * instantMbps + 0.60 * smoothedUlSpeed)
                        recordedUlSamples.add(smoothedUlSpeed)
                    }

                    val stepFrac = ((now - stepStartTime).toFloat() / stepDurationMs).coerceIn(0f, 1f)
                    _state.update {
                        it.copy(
                            liveSpeed = smoothedUlSpeed,
                            progressFraction = ulProgressBase + (stepFrac * ulProgressRange),
                            uploadSamples = recordedUlSamples.takeLast(30)
                        )
                    }
                    stepLastBytes = currentBytes
                    stepLastTime = now
                }
            }

            isStepActive.set(false)
            uploadWorker.cancel()
            cancelAllActiveCalls()
            isCancelled.set(false)
            ulProgressBase += ulProgressRange
        }

        val ulTotalElapsedSec = ((System.currentTimeMillis() - ulOverallStartTime) / 1000.0).coerceAtLeast(0.001)
        val finalUlBytes = totalUploadedBytes.get()

        val finalUploadMbps = if (finalUlBytes > 0L) {
            (finalUlBytes * 8.0) / (ulTotalElapsedSec * 1_000_000.0)
        } else {
            0.0
        }

        if (finalUlBytes == 0L) {
            _state.update {
                it.copy(
                    phase = TestPhase.ERROR,
                    liveSpeed = 0.0,
                    errorMessage = "ไม่สามารถยืนยันการอัปโหลดกับเซิร์ฟเวอร์ทดสอบได้ กรุณาลองใหม่"
                )
            }
            return@supervisorScope _state.value
        }

        // Warnings Check (Latency > 150ms or Jitter > 35ms)
        val warning = when {
            finalPing > 150 -> "ค่า HTTP Latency สูง (${finalPing}ms) อาจส่งผลต่อการเล่นเกมหรือการสนทนาเสียง"
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
                testDurationMs = System.currentTimeMillis() - testStartTime,
                currentStepText = "การทดสอบเสร็จสมบูรณ์ (อ้างอิงข้อมูลจริงจากเซิร์ฟเวอร์)"
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
                progressFraction = 0.0f,
                currentStepText = "ยกเลิกการทดสอบแล้ว"
            )
        }
    }

    fun reset() {
        cancelAllActiveCalls()
        _state.value = SpeedTestState(phase = TestPhase.IDLE, liveSpeed = 0.0)
    }
}
