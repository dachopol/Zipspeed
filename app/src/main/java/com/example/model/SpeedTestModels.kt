package com.example.model

enum class TestPhase {
    IDLE,
    TESTING_PING,
    TESTING_DOWNLOAD,
    TESTING_UPLOAD,
    COMPLETED,
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
    HOME,
    HISTORY,
    SETTINGS
}

data class SpeedTestState(
    val phase: TestPhase = TestPhase.IDLE,
    val liveSpeed: Double = 0.0,
    val progressFraction: Float = 0.0f,
    val pingMs: Int? = null,
    val jitterMs: Int? = null,
    val packetLossPercent: Double? = null,
    val downloadMbps: Double? = null,
    val uploadMbps: Double? = null,
    val errorMessage: String? = null
)
