package com.example.runningapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.runningapp.models.Run

@Database(
    version = 1,
    entities = [Run::class]
)
@TypeConverters(Converters::class)
abstract class RunsDatabase : RoomDatabase() {

    abstract fun runsDao(): RunsDao
}