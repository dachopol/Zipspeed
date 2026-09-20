package com.example.engine

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager

data class WifiSignalSnapshot(
    val rssiDbm: Int,
    val linkSpeedMbps: Int,
    val frequencyMhz: Int,
    val band: String
)

object WifiSignalReader {
    fun read(context: Context): WifiSignalSnapshot? {
        return runCatching {
            val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivity.activeNetwork ?: return null
            val capabilities = connectivity.getNetworkCapabilities(network) ?: return null
            if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null

            val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = wifi.connectionInfo ?: return null
            val rssi = info.rssi
            if (rssi <= -127 || rssi >= 0) return null

            val frequency = info.frequency
            val band = when {
                frequency in 2400..2500 -> "2.4 GHz"
                frequency in 4900..5900 -> "5 GHz"
                frequency in 5925..7125 -> "6 GHz"
                else -> "Wi-Fi"
            }

            WifiSignalSnapshot(
                rssiDbm = rssi,
                linkSpeedMbps = info.linkSpeed.coerceAtLeast(0),
                frequencyMhz = frequency.coerceAtLeast(0),
                band = band
            )
        }.getOrNull()
    }
}
