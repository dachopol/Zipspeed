package com.example.model

data class ServerInfo(
    val id: String,
    val name: String,
    val location: String,
    val countryCode: String,
    val isAnycast: Boolean = true,
    val subLocation: String = "Anycast Edge",
    val hostUrl: String = "https://speed.cloudflare.com/__down?bytes=0",
    val downloadUrl: String = "https://speed.cloudflare.com/__down",
    val uploadUrl: String = "https://speed.cloudflare.com/__up",
    val distanceKm: Int = 0,
    val basePingMs: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

val DEFAULT_SERVERS = listOf(
    ServerInfo(
        id = "cf_auto",
        name = "Cloudflare Anycast (Auto PoP)",
        location = "เส้นทางจริงเลือกโดย Anycast",
        countryCode = "GLOBAL",
        isAnycast = true,
        subLocation = "PoP จริงตรวจพบหลังเริ่มทดสอบ",
        basePingMs = 0,
        latitude = 0.0,
        longitude = 0.0
    )
)
