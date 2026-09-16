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
    val basePingMs: Int = 10,
    val latitude: Double = 13.7563,
    val longitude: Double = 100.5018
)

val DEFAULT_SERVERS = listOf(
    ServerInfo(
        id = "cf_auto",
        name = "Cloudflare Anycast (เร็วที่สุด / Auto PoP)",
        location = "ค้นหาโหนดใกล้ที่สุดอัตโนมัติ",
        countryCode = "GLOBAL",
        isAnycast = true,
        subLocation = "Anycast Routing (Auto PoP)"
    ),
    ServerInfo(
        id = "cf_asia_bkk",
        name = "Cloudflare Edge (South East Asia / BKK PoP)",
        location = "Southeast Asia Edge Node",
        countryCode = "TH",
        isAnycast = true,
        subLocation = "Bangkok Metro Transit"
    ),
    ServerInfo(
        id = "cf_asia_sin",
        name = "Cloudflare Edge (Singapore PoP)",
        location = "Singapore Regional Hub",
        countryCode = "SG",
        isAnycast = true,
        subLocation = "Singapore Equinix Node"
    ),
    ServerInfo(
        id = "cf_east_asia_hkg",
        name = "Cloudflare Edge (East Asia / HKG PoP)",
        location = "East Asia Gateway",
        countryCode = "HK",
        isAnycast = true,
        subLocation = "Hong Kong Core"
    ),
    ServerInfo(
        id = "cf_east_asia_nrt",
        name = "Cloudflare Edge (Japan / NRT PoP)",
        location = "Tokyo Transit Exchange",
        countryCode = "JP",
        isAnycast = true,
        subLocation = "Tokyo Otemachi Hub"
    ),
    ServerInfo(
        id = "cf_us_west",
        name = "Cloudflare Edge (US West Coast)",
        location = "North America West",
        countryCode = "US",
        isAnycast = true,
        subLocation = "San Jose / Los Angeles"
    ),
    ServerInfo(
        id = "cf_eu_central",
        name = "Cloudflare Edge (Europe Central)",
        location = "Europe Hub (DE/UK)",
        countryCode = "DE",
        isAnycast = true,
        subLocation = "Frankfurt / London PoP"
    )
)

