package com.example.runningapp.di

import androidx.room.Room
import com.example.runningapp.MainApp
import com.example.runningapp.database.RunsDao
import com.example.runningapp.database.RunsDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideRunsDatabase(app: MainApp): RunsDatabase =
        Room.databaseBuilder(app, RunsDatabase::class.java, "runs_database").build()

    @Provides
    fun provideRunsDao(database: RunsDatabase): RunsDao = database.runsDao()
}