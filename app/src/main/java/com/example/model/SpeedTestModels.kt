package com.example.model

enum class TestPhase {
    IDLE,
    TESTING_PING,
    TESTING_DOWNLOAD,
    TESTING_UPLOAD,
    COMPLETED,
    CANCELLED,
    ERROR
}

enum class SpeedUnit(val label: String) {
    MBPS("Mbps"),
    MB_S("MB/s")
}

enum class Language(val code: String, val displayName: String) {
    TH("th", "ไทย"),
    EN("en", "EN")
}

enum class NavTab {
    SPEED,
    VIDEO,
    STATUS,
    MAP,
    HISTORY,
    SETTINGS,
    AD_FREE
}

data class SpeedTestState(
    val phase: TestPhase = TestPhase.IDLE,
    val liveSpeed: Double = 0.0,
    val progressFraction: Float = 0.0f,
    val pingMs: Int? = null,
    val jitterMs: Int? = null,
    val packetLossPercent: Double? = null,
    val downloadLatencyMs: Int? = null,
    val uploadLatencyMs: Int? = null,
    val downloadMbps: Double? = null,
    val uploadMbps: Double? = null,
    val errorMessage: String? = null,
    val downloadSamples: List<Double> = emptyList(),
    val uploadSamples: List<Double> = emptyList(),
    val detectedColo: String? = null,
    val detectedClientIp: String? = null,
    val detectedAsn: String? = null,
    val warningMessage: String? = null,
    val bytesDownloaded: Long = 0L,
    val bytesUploaded: Long = 0L,
    val testDurationMs: Long = 0L,
    val isPrecisionMode: Boolean = false
)
