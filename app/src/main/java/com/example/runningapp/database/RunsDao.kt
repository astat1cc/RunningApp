package com.example.runningapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.runningapp.models.Run

@Dao
interface RunsDao {

    @Insert
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT * FROM runs_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM runs_table ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    @Query("SELECT * FROM runs_table ORDER BY runTimeInMillis DESC")
    fun getAllRunsSortedByRunTime(): LiveData<List<Run>>

    @Query("SELECT * FROM runs_table ORDER BY avgSpeedInKPH DESC")
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM runs_table ORDER BY burnedCalories DESC")
    fun getAllRunsSortedByBurnedCalories(): LiveData<List<Run>>

    @Query("SELECT SUM(distanceInMeters) FROM runs_table")
    fun getTotalDistanceStatistics(): LiveData<Int>

    @Query("SELECT SUM(runTimeInMillis) FROM runs_table")
    fun getTotalRunTimeStatistics(): LiveData<Long>

    @Query("SELECT SUM(avgSpeedInKPH) FROM runs_table")
    fun getTotalAvgSpeedStatistics(): LiveData<Double>

    @Query("SELECT SUM(burnedCalories) FROM runs_table")
    fun getTotalBurnedCaloriesStatistics(): LiveData<Int>
}