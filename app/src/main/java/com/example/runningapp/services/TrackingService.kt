package com.example.runningapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.runningapp.R
import com.example.runningapp.appComponent
import com.example.runningapp.utilities.Constants.ACTION_FINISH_TRACKING_SERVICE
import com.example.runningapp.utilities.Constants.ACTION_PAUSE_TRACKING_SERVICE
import com.example.runningapp.utilities.Constants.ACTION_RESUME_TRACKING_SERVICE
import com.example.runningapp.utilities.Constants.ACTION_START_TRACKING_SERVICE
import com.example.runningapp.utilities.Constants.LOCATION_TRACKING_REQUEST_FASTEST_INTERVAL
import com.example.runningapp.utilities.Constants.LOCATION_TRACKING_REQUEST_INTERVAL
import com.example.runningapp.utilities.Constants.TIMER_UPDATE_INTERVAL
import com.example.runningapp.utilities.Constants.TRACKING_SERVICE_NOTIFICATION_ID
import com.example.runningapp.utilities.Constants.TRACKING_SERVICE_NOTIFICATION_CHANNEL_ID
import com.example.runningapp.utilities.Constants.TRACKING_SERVICE_NOTIFICATION_CHANNEL_NAME
import com.example.runningapp.utilities.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias LapPath = MutableList<LatLng> // run from pressing start/resume to stop
typealias WholeRunSessionPath = MutableList<LapPath> // all runs from pressing start to finish

class TrackingService : LifecycleService() {

    private val runTimeInSeconds: MutableLiveData<Int> = MutableLiveData(0)
    var isServiceAlive = false

    companion object {
        val isTracking: MutableLiveData<Boolean> = MutableLiveData(false)
        val wholeRunSessionPath: MutableLiveData<WholeRunSessionPath> =
            MutableLiveData(mutableListOf())
        val runTimeInMillis: MutableLiveData<Long> = MutableLiveData(0L)
        val isStarted: MutableLiveData<Boolean> = MutableLiveData(false)
    }

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private lateinit var currentNotificationBuilder: NotificationCompat.Builder


    private var shouldStartNewLap = true

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.forEach { location ->
                val position = LatLng(location.latitude, location.longitude)
                addPathPoint(position)
            }
        }
    }

    private var lapTimeInMillis = 0L
    private var wholeRunTimeInMillis = 0L
    private var timeLapStartedInMillis = 0L

    private fun toggleTimer(isTracking: Boolean) {
        if (!isTracking) return
        timeLapStartedInMillis = System.currentTimeMillis()
        lifecycleScope.launch(Dispatchers.Main) {
            while (TrackingService.isTracking.value!!) {
                lapTimeInMillis = System.currentTimeMillis() - timeLapStartedInMillis
                runTimeInMillis.postValue(wholeRunTimeInMillis + lapTimeInMillis)

                if (runTimeInMillis.value!! >= runTimeInSeconds.value!! * 1000L + 1000L) {
                    runTimeInSeconds.postValue(runTimeInSeconds.value!! + 1)
                }

                delay(TIMER_UPDATE_INTERVAL)
            }
            wholeRunTimeInMillis += lapTimeInMillis
        }
    }

    private fun addPathPoint(coordinates: LatLng) {
        if (shouldStartNewLap) {
            wholeRunSessionPath.value?.apply {
                add(mutableListOf())
                shouldStartNewLap = false
                wholeRunSessionPath.postValue(this)
            }
        }
        wholeRunSessionPath.value?.apply {
            last().add(coordinates)
            wholeRunSessionPath.postValue(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun toggleLocationTracking(isTracking: Boolean) {
        if (!isTracking) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            return
        }
        if (TrackingUtility.hasAllLocationPermissions(this)) {
            val request = LocationRequest.create().apply {
                priority = PRIORITY_HIGH_ACCURACY
                interval = LOCATION_TRACKING_REQUEST_INTERVAL
                fastestInterval = LOCATION_TRACKING_REQUEST_FASTEST_INTERVAL
            }
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder = baseNotificationBuilder

        isTracking.observe(this, Observer { isTracking ->
            toggleLocationTracking(isTracking)
            toggleTimer(isTracking)
            updateNotificationAction(isTracking)
        })
        runTimeInSeconds.observe(this, Observer {
            if (!isServiceAlive) return@Observer
            updateNotificationTimerText(it)
        })
        isStarted.observe(this, Observer {
            if (it) isServiceAlive = true
        })
    }

    private fun updateNotificationTimerText(runTimeInSeconds: Int) {
        val notification = currentNotificationBuilder
            .setContentText(TrackingUtility.getStopWatchFormatFromMillis(runTimeInSeconds * 1000L))
            .build()
        if (isServiceAlive) {
            notificationManager.notify(TRACKING_SERVICE_NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotificationAction(isTracking: Boolean = true) {
        val pendingIntent = getTrackingServicePendingIntent(isTracking)
        val (actionTitle, actionIcon) = if (isTracking) {
            "Pause" to R.drawable.ic_pause_black_24dp
        } else {
            "Resume" to R.drawable.ic_play_arrow_24
        }

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        val newNotification = currentNotificationBuilder
            .addAction(actionIcon, actionTitle, pendingIntent)
            .build()

        if (isServiceAlive) {
            notificationManager.notify(TRACKING_SERVICE_NOTIFICATION_ID, newNotification)
        }
    }

    private fun getTrackingServicePendingIntent(isTracking: Boolean): PendingIntent {
        val trackingServiceIntent = Intent(this, TrackingService::class.java).also {
            it.action =
                if (isTracking) ACTION_PAUSE_TRACKING_SERVICE else ACTION_RESUME_TRACKING_SERVICE
        }
        return PendingIntent.getService(
            this,
            0,
            trackingServiceIntent,
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                FLAG_UPDATE_CURRENT
            } else {
                FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
            }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_TRACKING_SERVICE -> {
                    startTrackingService()
                }
                ACTION_PAUSE_TRACKING_SERVICE -> {
                    pauseTrackingService()
                }
                ACTION_RESUME_TRACKING_SERVICE -> {
                    resumeTrackingService()
                }
                ACTION_FINISH_TRACKING_SERVICE -> {
                    finishTrackingService()
                }
                else -> {

                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun finishTrackingService() {
        isTracking.postValue(false)
        resetInitialValues()
        stopForeground(true)
        stopSelf()
    }

    private fun resetInitialValues() {
        isStarted.postValue(false)
        runTimeInMillis.postValue(0L)
        runTimeInSeconds.postValue(0)
        wholeRunSessionPath.postValue(mutableListOf())
        wholeRunTimeInMillis = 0L
        lapTimeInMillis = 0L
        isServiceAlive = false
    }

    private fun resumeTrackingService() {
        isTracking.postValue(true)
    }

    private fun pauseTrackingService() {
        isTracking.postValue(false)
    }

    private fun startTrackingService() {
        isStarted.postValue(true)
        isTracking.postValue(true)
        createNotificationChannel(notificationManager)

        val notification = baseNotificationBuilder.build()

        startForeground(TRACKING_SERVICE_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            TRACKING_SERVICE_NOTIFICATION_CHANNEL_ID,
            TRACKING_SERVICE_NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}