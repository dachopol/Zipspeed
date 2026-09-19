package com.example.engine

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class GpsLocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val regionTag: String,
    val province: String,
    val isRealGps: Boolean = true
)

class GpsLocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    suspend fun getCurrentLocation(): GpsLocationData? {
        if (!hasLocationPermission()) return null

        return try {
            kotlinx.coroutines.withTimeoutOrNull(3000L) {
                val location = fetchFusedLocation()
                if (location != null) {
                    mapLocationToRegion(location, isReal = true)
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("MissingPermission")
    private suspend fun fetchFusedLocation(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            val cts = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (continuation.isActive) {
                        if (loc != null) {
                            continuation.resume(loc)
                        } else {
                            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                                if (continuation.isActive) continuation.resume(lastLoc)
                            }.addOnFailureListener {
                                if (continuation.isActive) continuation.resume(null)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }

            continuation.invokeOnCancellation {
                cts.cancel()
            }
        } catch (_: SecurityException) {
            if (continuation.isActive) continuation.resume(null)
        }
    }

    fun mapLocationToRegion(loc: Location, isReal: Boolean): GpsLocationData {
        val lat = loc.latitude
        val lon = loc.longitude
        val acc = loc.accuracy

        val (province, regionTag) = when {
            lat in 13.4..13.7 && lon in 100.1..100.4 -> Pair("สมุทรสาคร", "สมุทรสาคร (GPS จริง)")
            lat in 13.6..13.9 && lon in 100.4..100.7 -> Pair("กรุงเทพมหานคร", "กรุงเทพฯ (GPS จริง)")
            lat in 13.8..14.1 && lon in 100.3..100.7 -> Pair("นนทบุรี / ปทุมธานี", "นนทบุรี (GPS จริง)")
            lat in 13.4..13.7 && lon in 100.5..100.9 -> Pair("สมุทรปราการ", "สมุทรปราการ (GPS จริง)")
            lat in 12.8..13.4 && lon in 100.8..101.2 -> Pair("ชลบุรี", "ชลบุรี (GPS จริง)")
            lat in 18.5..19.2 && lon in 98.8..99.3 -> Pair("เชียงใหม่", "เชียงใหม่ (GPS จริง)")
            lat in 16.0..16.8 && lon in 102.5..103.2 -> Pair("ขอนแก่น", "ขอนแก่น (GPS จริง)")
            lat in 7.0..7.4 && lon in 100.3..100.7 -> Pair("สงขลา / หาดใหญ่", "สงขลา (GPS จริง)")
            else -> {
                val formattedLat = String.format(Locale.US, "%.3f", lat)
                val formattedLon = String.format(Locale.US, "%.3f", lon)
                Pair("พิกัด GPS", "$formattedLat°N, $formattedLon°E")
            }
        }

        return GpsLocationData(
            latitude = lat,
            longitude = lon,
            accuracyMeters = acc,
            regionTag = regionTag,
            province = province,
            isRealGps = isReal
        )
    }
}
