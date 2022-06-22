package com.example.runningapp.di

import android.content.Context.MODE_PRIVATE
import com.example.runningapp.MainApp
import com.example.runningapp.utilities.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides

@Module(includes = [DatabaseModule::class, TrackingServiceModule::class])
class AppModule(val app: MainApp) {

    @Provides
    fun provideMainApp(): MainApp = app

    @Provides
    fun provideSharedPref(app: MainApp) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
}