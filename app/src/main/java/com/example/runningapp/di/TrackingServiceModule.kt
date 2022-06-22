package com.example.runningapp.di

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.runningapp.MainApp
import com.example.runningapp.R
import com.example.runningapp.ui.MainActivity
import com.example.runningapp.utilities.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides

@Module
class TrackingServiceModule {

    @Provides
    fun provideFusedLocationProviderClient(app: MainApp): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(app)

    @Provides
    fun provideMainActivityPendingIntent(app: MainApp): PendingIntent {
        val mainActivityIntent = Intent(app, MainActivity::class.java).also {
            it.action = Constants.ACTION_TRACKING_SERVICE_NOTIFICATION_PRESSED
        }
        return PendingIntent.getActivity(
            app,
            0,
            mainActivityIntent,
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
    }

    @Provides
    fun provideTrackingNotificationBaseBuilder(
        app: MainApp,
        mainActivityPendingIntent: PendingIntent
    ): NotificationCompat.Builder = NotificationCompat.Builder(
            app,
            Constants.TRACKING_SERVICE_NOTIFICATION_CHANNEL_ID
        )
            .setAutoCancel(false)
            .setContentTitle("Current Run")
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setOngoing(true)
            .setContentText("00:00:00")
            .setContentIntent(mainActivityPendingIntent)
}