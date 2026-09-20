package com.example.engine

import com.example.model.VideoResolution
import com.example.model.VideoTestState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.math.min

class VideoPerformanceTester {

    private val client = OkHttpClient.Builder()
        .connectTimeout(1200, TimeUnit.MILLISECONDS)
        .readTimeout(1500, TimeUnit.MILLISECONDS)
        .callTimeout(1800, TimeUnit.MILLISECONDS)
        .build()

    private val _state = MutableStateFlow(VideoTestState())
    val state: StateFlow<VideoTestState> = _state.asStateFlow()

    @Volatile
    private var activeCall: Call? = null

    suspend fun runVideoTest(batterySaver: Boolean = false): VideoTestState = coroutineScope {
        cancelTest()
        _state.value = VideoTestState(
            isTesting = true,
            progress = 0.04f,
            currentResolution = VideoResolution.SD_480P,
            isCompleted = false
        )

        val testResolutions = listOf(
            VideoResolution.SD_480P,
            VideoResolution.HD_720P,
            VideoResolution.FHD_1080P,
            VideoResolution.QHD_1440P,
            VideoResolution.UHD_4K
        )

        var maxPassed: VideoResolution? = null
        var lastAchievedBitrate = 0.0
        var totalBufferTimeMs = 0
        var totalLoadTimeMs = 0
        val baseStepDurationMs = if (batterySaver) 700L else 850L

        for ((index, res) in testResolutions.withIndex()) {
            if (!isActive || !_state.value.isTesting) break

            _state.update {
                it.copy(
                    currentResolution = res,
                    progress = (index.toFloat() + 0.08f) / testResolutions.size
                )
            }

            val targetBytes = (res.minMbps * 125_000 * 0.7).toLong().coerceIn(200_000L, 900_000L)
            val speedUrl = "https://speed.cloudflare.com/__down?bytes=$targetBytes"

            var bytesRead = 0L
            var bufferTime = 0
            val resolutionStartTime = System.currentTimeMillis()

            // Safe bounded execution with instant call cancellation on timeout
            withTimeoutOrNull(baseStepDurationMs + 200L) {
                try {
                    val request = Request.Builder()
                        .url(speedUrl)
                        .header("User-Agent", "Zipspeed/35.0")
                        .header("Cache-Control", "no-cache")
                        .build()

                    val startReq = System.currentTimeMillis()
                    val call = client.newCall(request)
                    activeCall = call

                    // Ensure call is aborted immediately if coroutine scope completes/cancels
                    val cancelHandler = coroutineContext.job.invokeOnCompletion {
                        try { call.cancel() } catch (_: Exception) {}
                    }

                    try {
                        withContext(Dispatchers.IO) {
                            call.execute().use { response ->
                                bufferTime = (System.currentTimeMillis() - startReq).toInt().coerceIn(12, 180)
                                val body = response.body
                                if (body != null) {
                                    val stream = body.byteStream()
                                    val buffer = ByteArray(16 * 1024)
                                    val streamStartTime = System.currentTimeMillis()

                                    while (isActive && _state.value.isTesting) {
                                        val read = stream.read(buffer)
                                        if (read == -1) break
                                        bytesRead += read

                                        val now = System.currentTimeMillis()
                                        val curElapsedSec = (now - streamStartTime) / 1000.0
                                        if (curElapsedSec > 0.04) {
                                            val liveMbps = (bytesRead * 8.0) / (curElapsedSec * 1_000_000.0)
                                            val streamProgress = (index.toFloat() + (bytesRead.toFloat() / targetBytes).coerceIn(0.1f, 0.95f)) / testResolutions.size
                                            _state.update {
                                                it.copy(
                                                    streamBitrateMbps = min(liveMbps, 180.0),
                                                    progress = streamProgress,
                                                    bufferTimeMs = bufferTime
                                                )
                                            }
                                        }

                                        if (now - streamStartTime > baseStepDurationMs || bytesRead >= targetBytes) {
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    } finally {
                        cancelHandler.dispose()
                        activeCall = null
                    }
                } catch (_: Exception) {
                    activeCall = null
                }
            }

            // Real-data rule: no simulated fallback. A blocked/unreachable endpoint remains 0 Mbps.
            if (bytesRead <= 0L) {
                _state.update {
                    it.copy(
                        streamBitrateMbps = 0.0,
                        bufferTimeMs = bufferTime
                    )
                }
            }

            totalBufferTimeMs += bufferTime
            val elapsedSec = ((System.currentTimeMillis() - resolutionStartTime) / 1000.0).coerceAtLeast(0.4)
            val measuredMbps = if (bytesRead > 0) (bytesRead * 8.0) / (elapsedSec * 1_000_000.0) else 0.0
            lastAchievedBitrate = min(measuredMbps, 180.0)
            totalLoadTimeMs += (elapsedSec * 1000).toInt()

            // Pass condition: Achieved sustained bitrate >= 70% of minimum requirement for this resolution
            if (lastAchievedBitrate >= res.minMbps * 0.70) {
                maxPassed = res
            }

            val currentBufferAvg = if (index > 0) totalBufferTimeMs / (index + 1) else bufferTime
            _state.update {
                it.copy(
                    progress = (index + 1.0f) / testResolutions.size,
                    bufferTimeMs = currentBufferAvg,
                    loadTimeMs = totalLoadTimeMs,
                    streamBitrateMbps = lastAchievedBitrate,
                    maxResolutionPassed = maxPassed
                )
            }

            delay(if (batterySaver) 80L else 40L)
        }

        val qualityScore = when (maxPassed) {
            VideoResolution.UHD_4K -> 95 + (min(lastAchievedBitrate, 60.0) / 12.0).toInt().coerceIn(0, 5)
            VideoResolution.QHD_1440P -> 82 + (min(lastAchievedBitrate, 30.0) / 3.0).toInt().coerceIn(0, 12)
            VideoResolution.FHD_1080P -> 68 + (min(lastAchievedBitrate, 18.0) / 2.0).toInt().coerceIn(0, 12)
            VideoResolution.HD_720P -> 52 + (min(lastAchievedBitrate, 8.0)).toInt().coerceIn(0, 12)
            VideoResolution.SD_480P -> 38
            null -> 25
        }

        val finalState = _state.value.copy(
            isTesting = false,
            progress = 1.0f,
            isCompleted = true,
            maxResolutionPassed = maxPassed,
            qualityScore = qualityScore
        )
        _state.value = finalState
        finalState
    }

    fun cancelTest() {
        try {
            activeCall?.cancel()
        } catch (_: Exception) {}
        activeCall = null
        _state.value = _state.value.copy(isTesting = false)
    }

    fun reset() {
        cancelTest()
        _state.value = VideoTestState()
    }
}
