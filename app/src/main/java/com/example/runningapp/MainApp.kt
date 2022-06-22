package com.example.runningapp

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import com.example.runningapp.di.AppComponent
import com.example.runningapp.di.AppModule
import com.example.runningapp.di.DaggerAppComponent

class MainApp : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder().appModule(AppModule(this)).build()
    }
}

val Context.appComponent: AppComponent
    get() = when (this) {
            is MainApp -> appComponent
            else -> applicationContext.appComponent
        }

val Fragment.appComponent: AppComponent
    get() = requireContext().appComponent