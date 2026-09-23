package com.example.model

enum class SubTestMode {
    SPEED,
    VIDEO,
    WEB
}

// Video Streaming Performance Test Models (Real Network Payload)
enum class VideoResolution(val label: String, val minMbps: Double, val badgeText: String) {
    SD_480P("480p SD", 2.5, "Standard Definition"),
    HD_720P("720p HD", 5.0, "High Definition"),
    FHD_1080P("1080p FHD", 10.0, "Full HD"),
    QHD_1440P("1440p 2K", 20.0, "Quad HD 2K"),
    UHD_4K("2160p 4K", 35.0, "Ultra HD 4K")
}

data class VideoTestState(
    val isTesting: Boolean = false,
    val progress: Float = 0f,
    val currentResolution: VideoResolution = VideoResolution.SD_480P,
    val bufferTimeMs: Int = 0,
    val loadTimeMs: Int = 0,
    val streamBitrateMbps: Double = 0.0,
    val maxResolutionPassed: VideoResolution? = null,
    val isCompleted: Boolean = false,
    val qualityScore: Int = 0
)

// Web Browsing & CDN Performance Test Models (Real HTTP Checks)
data class WebSiteTest(
    val id: String,
    val name: String,
    val domain: String,
    val category: String,
    val latencyMs: Int = 0,
    val ttfbMs: Int = 0,
    val statusCode: Int = 0,
    val statusText: String = "Pending",
    val isSuccess: Boolean = false
)

data class WebTestState(
    val isTesting: Boolean = false,
    val overallScore: Int = 0,
    val sites: List<WebSiteTest> = emptyList(),
    val isCompleted: Boolean = false
)

// Mobile Performance & Device Info
data class MobilePerformanceState(
    val batteryLevel: Int = 0,
    val cpuTempC: Double = 0.0,
    val ramUsagePercent: Int = 0,
    val signalStrengthDbm: Int = 0,
    val signalDbm: Int = 0,
    val deviceTemperatureC: Double = 0.0,
    val bufferbloatMs: Int = 0,
    val networkType: String = "ไม่มีข้อมูล",
    val isRoaming: Boolean = false
)

// Outage & Downdetector Models
enum class OutageLevel(val label: String) {
    NORMAL("ปกติ"),
    DEGRADED("มีปัญหาเล็กน้อย"),
    OUTAGE("ล่มบางพื้นที่"),
    MAJOR("ล่มรุนแรง")
}

data class DowndetectorService(
    val id: String,
    val name: String,
    val serviceType: String,
    val status: OutageLevel,
    val incidentCount24h: Int,
    val reportsTimeline: List<Int>,
    val lastUpdatedText: String,
    val userReported: Boolean = false
)

// ISP Benchmarking Models
data class IspComparisonBenchmark(
    val ispName: String,
    val region: String,
    val avgDownloadMbps: Double,
    val avgUploadMbps: Double,
    val avgLatencyMs: Int,
    val consistencyScore: Double,
    val rankLabel: String
)

// Signal Scanner & Spatial Mapping
data class SignalMapPoint(
    val id: String = java.util.UUID.randomUUID().toString(),
    val x: Double,
    val y: Double,
    val dbm: Int,
    val zoneName: String = "",
    val linkSpeedMbps: Int = 0,
    val stepIndex: Int = 0
)

data class SignalAlertEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val dbm: Int = -85,
    val zoneName: String = "",
    val threshold: Int = -80,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ScannerExecutionStatus {
    ACTIVE,
    STANDBY,
    RUNNING,
    DISABLED,
    PAUSED_SCHEDULE,
    PAUSED_BATTERY,
    PAUSED_BATTERY_LOW,
    PAUSED_OUTSIDE_SCHEDULE
}

enum class SchedulePreset {
    ALL_DAY,
    DAYTIME,
    WORK_HOURS,
    NIGHT_SHIFT,
    CUSTOM
}

data class ScannerScheduleConfig(
    val scheduleEnabled: Boolean = false,
    val preset: SchedulePreset = SchedulePreset.ALL_DAY,
    val startHour: Int = 8,
    val startMinute: Int = 0,
    val endHour: Int = 22,
    val endMinute: Int = 0,
    val minBatteryThresholdPercent: Int = 20,
    val bypassBatteryWhenCharging: Boolean = true
)

data class SignalScannerState(
    val currentDbm: Int = 0,
    val channelWidthMhz: Int = 0,
    val channel: Int = 0,
    val band: String = "ไม่มีข้อมูล",
    val linkSpeedMbps: Int = 0,
    val routerX: Double? = null,
    val routerY: Double? = null,
    val movementPoints: List<SignalMapPoint> = emptyList(),
    val activeZone: String = "ไม่มีข้อมูล",
    val isRecordingMovement: Boolean = false,
    val alertNotificationsEnabled: Boolean = true,
    val alertThresholdDbm: Int = -80,
    val alertHistory: List<SignalAlertEvent> = emptyList(),
    val lastAlertEvent: SignalAlertEvent? = null,
    val backgroundScanEnabled: Boolean = false,
    val scheduleConfig: ScannerScheduleConfig = ScannerScheduleConfig(),
    val currentBatteryPercent: Int = 80,
    val isDeviceCharging: Boolean = false,
    val executionStatus: ScannerExecutionStatus = ScannerExecutionStatus.STANDBY
)
