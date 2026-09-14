package com.example.model

data class ServerInfo(
    val id: String,
    val name: String,
    val location: String,
    val countryCode: String,
    val basePingMs: Int,
    val distanceKm: Int = 12,
    val subLocation: String = "Samut Sakhon",
    val latitude: Double = 13.7563,
    val longitude: Double = 100.5018,
    val hostUrl: String = "https://1.1.1.1"
)

val DEFAULT_SERVERS = listOf(
    ServerInfo("auto_best", "Auto (Fastest Server)", "Bangkok, Thailand", "TH", 10, 12, "Samut Sakhon", 13.7563, 100.5018, "https://speed.cloudflare.com/__down?bytes=0"),
    ServerInfo("th_bkk_1", "AIS Fiber Server", "Bangkok, Thailand", "TH", 11, 12, "Samut Sakhon", 13.7563, 100.5018, "https://speed.cloudflare.com/__down?bytes=0"),
    ServerInfo("th_bkk_2", "True Online Speedtest", "Bangkok, Thailand", "TH", 14, 18, "Nonthaburi", 13.8591, 100.5217, "https://speed.cloudflare.com/__down?bytes=0"),
    ServerInfo("sg_sin_1", "Cloudflare Edge SG", "Singapore", "SG", 24, 1420, "Jurong East", 1.3521, 103.8198, "https://speed.cloudflare.com/__down?bytes=0"),
    ServerInfo("hk_hkg_1", "Equinix HK1 Center", "Hong Kong", "HK", 42, 2310, "Kwai Chung", 22.3193, 114.1694, "https://speed.cloudflare.com/__down?bytes=0"),
    ServerInfo("jp_tyo_1", "NTT Communications", "Tokyo, Japan", "JP", 62, 4320, "Otemachi", 35.6762, 139.6503, "https://speed.cloudflare.com/__down?bytes=0"),
    ServerInfo("au_syd_1", "AWS Oceania", "Sydney, Australia", "AU", 135, 7530, "Sydney CBD", -33.8688, 151.2093, "https://speed.cloudflare.com/__down?bytes=0"),
    ServerInfo("de_fra_1", "AWS EU Central", "Frankfurt, Germany", "DE", 175, 8950, "Frankfurt am Main", 50.1109, 8.6821, "https://speed.cloudflare.com/__down?bytes=0"),
    ServerInfo("uk_lon_1", "Linode London Edge", "London, United Kingdom", "UK", 182, 9540, "Docklands", 51.5074, -0.1278, "https://speed.cloudflare.com/__down?bytes=0"),
    ServerInfo("us_lax_1", "Fastly West Coast", "Los Angeles, USA", "US", 188, 13200, "Los Angeles", 34.0522, -118.2437, "https://speed.cloudflare.com/__down?bytes=0"),
    ServerInfo("us_nyc_1", "DigitalOcean NY3", "New York, USA", "US", 210, 13900, "New York City", 40.7128, -74.0060, "https://speed.cloudflare.com/__down?bytes=0")
)
