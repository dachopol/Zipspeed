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
    val statusCode: Int = 200,
    val statusText: String = "Pending",
    val isSuccess: Boolean = false
)

data class WebTestState(
    val isTesting: Boolean = false,
    val overallScore: Int = 0,
    val sites: List<WebSiteTest> = emptyList(),
    val isCompleted: Boolean = false
)
