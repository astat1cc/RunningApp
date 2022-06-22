package com.example.runningapp.utilities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.example.runningapp.services.WholeRunSessionPath

object TrackingUtility {

    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun hasAllLocationPermissions(context: Context) =
        LOCATION_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    fun getStopWatchFormatFromMillis(ms: Long, includeMillis: Boolean = false): String {
        val hours = ms / 1000 / 60 / 60
        val minutes = ms / 1000 / 60 % 60
        val seconds = ms / 1000 % 60
        var stopWatchTime = "${if (hours < 10) "0" else ""}$hours" +
                ":${if (minutes < 10) "0" else ""}$minutes" +
                ":${if (seconds < 10) "0" else ""}$seconds"
        if (includeMillis) {
            val millis = ms % 1000 / 10 // divided by 10 because we need 2 digit millis view in stopwatch
            stopWatchTime += ":${if (millis < 10) "0" else ""}$millis"
        }
        return stopWatchTime
    }

    fun calculateRunPathDistance(runSessionPath: WholeRunSessionPath): Int {
        var distance = 0.0
        runSessionPath.forEach { lap ->
            lap.windowed(2, 1).forEach { latLngPair ->
                val pos1 = latLngPair.first()
                val pos2 = latLngPair.last()
                val result = FloatArray(1)
                Location.distanceBetween(
                    pos1.latitude,
                    pos1.longitude,
                    pos2.latitude,
                    pos2.longitude,
                    result
                )
                distance += result.first()
            }
        }
        return distance.toInt()
    }
}