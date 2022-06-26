package com.example.runningapp.di

import com.example.runningapp.service.TrackingService
import com.example.runningapp.ui.MainActivity
import com.example.runningapp.ui.fragments.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(fragment: RunsFragment)

    fun inject(service: TrackingService)

    fun inject(fragment: TrackingFragment)

    fun inject(fragment: SetupFragment)

    fun inject(activity: MainActivity)

    fun inject(fragment: StatisticsFragment)

    fun inject(fragment: SettingsFragment)
}