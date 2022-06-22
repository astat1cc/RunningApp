package com.example.runningapp.models

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "runs_table")
class Run(
    val distanceInMeters: Int = 0,
    val runTimeInMillis: Long = 0L,
    val burnedCalories: Int = 0,
    val avgSpeedInKPH: Double = 0.0,
    val timestamp: Long = 0L,
    var image: Bitmap? = null
) {
    @PrimaryKey(autoGenerate = true) var id: Int? = null
}