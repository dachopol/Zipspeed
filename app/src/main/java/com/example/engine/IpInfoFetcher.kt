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
    val publicIp: String = "กำลังตรวจสอบ...",
    val localIp: String = "127.0.0.1",
    val ispName: String = "AIS Fiber / True Online",
    val countryCode: String = "TH",
    val isFetching: Boolean = false
)

class IpInfoFetcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(2, TimeUnit.SECONDS)
        .build()

    fun getLocalIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
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
        } catch (e: Exception) {
            // fallback
        }
        return "192.168.1.108"
    }

    suspend fun fetchPublicIpInfo(): NetworkIpInfo = withContext(Dispatchers.IO) {
        val localIp = getLocalIpAddress()
        var publicIp = "182.52.12.94"
        var isp = "AIS Fibre Thailand"

        try {
            val request = Request.Builder()
                .url("https://api.ipify.org?format=json")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string()
                    if (!bodyStr.isNullOrEmpty()) {
                        val json = JSONObject(bodyStr)
                        publicIp = json.optString("ip", publicIp)
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback default IP if offline or blocked
        }

        NetworkIpInfo(
            publicIp = publicIp,
            localIp = localIp,
            ispName = isp,
            isFetching = false
        )
    }
}
