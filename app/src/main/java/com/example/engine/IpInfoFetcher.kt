package com.example.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.NetworkInterface
import java.util.Collections
import java.util.concurrent.TimeUnit

data class NetworkIpInfo(
    val publicIp: String? = null,
    val localIp: String? = null,
    val ispName: String? = null,
    val countryCode: String? = null,
    val city: String? = null,
    val colo: String? = null,
    val isFetching: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val regionTag: String? = null,
    val province: String? = null,
    val isGpsActive: Boolean = false,
    val locationStatusText: String = "รอตรวจสอบการเชื่อมต่อ..."
)

class IpInfoFetcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .build()

    fun getLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.isUp && !intf.isLoopback) {
                    val addrs = Collections.list(intf.inetAddresses)
                    for (addr in addrs) {
                        if (!addr.isLoopbackAddress) {
                            val sAddr = addr.hostAddress
                            if (sAddr != null && sAddr.indexOf(':') < 0) {
                                return sAddr
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }

    suspend fun fetchPublicIpInfo(): NetworkIpInfo = withContext(Dispatchers.IO) {
        val localIp = getLocalIpAddress()
        var publicIp: String? = null
        var isp: String? = null
        var countryCode: String? = null
        var city: String? = null
        var colo: String? = null
        var latitude: Double? = null
        var longitude: Double? = null

        // Method 1: Query Cloudflare speed edge for real edge metadata
        try {
            val request = Request.Builder()
                .url("https://speed.cloudflare.com/__down?bytes=0")
                .header("User-Agent", "Zipspeed/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    // cf-ray is a request identifier, not a client IP address.
                    publicIp = response.header("cf-meta-ip")
                    val asn = response.header("asn") ?: response.header("cf-meta-asn")
                    city = response.header("city") ?: response.header("cf-meta-city")
                    countryCode = response.header("country") ?: response.header("cf-meta-country")
                    colo = response.header("colo") ?: response.header("cf-meta-colo")

                    val latStr = response.header("latitude") ?: response.header("cf-meta-latitude")
                    val lonStr = response.header("longitude") ?: response.header("cf-meta-longitude")
                    latitude = latStr?.toDoubleOrNull()
                    longitude = lonStr?.toDoubleOrNull()

                    if (!asn.isNullOrBlank()) {
                        isp = "AS$asn Network"
                    }
                }
            }
        } catch (_: Exception) {}

        // Method 2: If IP still null, try ipify fallback
        if (publicIp.isNullOrBlank()) {
            try {
                val request = Request.Builder()
                    .url("https://api.ipify.org?format=json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyStr = response.body?.string()
                        if (!bodyStr.isNullOrEmpty()) {
                            val json = JSONObject(bodyStr)
                            publicIp = json.optString("ip", null)
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        val regionTag = when {
            !city.isNullOrBlank() && !colo.isNullOrBlank() -> "$city ($colo Edge)"
            !colo.isNullOrBlank() -> "Cloudflare $colo PoP"
            !city.isNullOrBlank() -> city
            else -> null
        }

        val statusText = if (!publicIp.isNullOrBlank()) {
            "เชื่อมต่อสำเร็จ: ${colo ?: "Edge"}"
        } else {
            "ไม่สามารถดึง Public IP ได้ (ออฟไลน์ / ไม่พร้อมใช้งาน)"
        }

        NetworkIpInfo(
            publicIp = publicIp,
            localIp = localIp,
            ispName = isp,
            countryCode = countryCode,
            city = city,
            colo = colo,
            latitude = latitude,
            longitude = longitude,
            regionTag = regionTag,
            isFetching = false,
            isGpsActive = false,
            locationStatusText = statusText
        )
    }
}

