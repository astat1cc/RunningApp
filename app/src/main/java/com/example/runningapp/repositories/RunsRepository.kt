package com.example.runningapp.repositories

import com.example.runningapp.database.RunsDatabase
import com.example.runningapp.models.Run
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunsRepository @Inject constructor(
    val database: RunsDatabase
) {

    suspend fun saveRun(run: Run) {
        withContext(Dispatchers.IO) {
            database.runsDao().insertRun(run)
        }
    }

    fun getAllRunsSortedByDate() = database.runsDao().getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = database.runsDao().getAllRunsSortedByDistance()

    fun getAllRunsSortedByAvgSpeed() = database.runsDao().getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByRunTime() = database.runsDao().getAllRunsSortedByRunTime()

    fun getAllRunsSortedByBurnedCalories() = database.runsDao().getAllRunsSortedByBurnedCalories()

    fun getTotalDistanceStatistics() = database.runsDao().getTotalDistanceStatistics()

    fun getTotalRunTimeStatistics() = database.runsDao().getTotalRunTimeStatistics()

    fun getTotalAvgSpeedStatistics() = database.runsDao().getTotalAvgSpeedStatistics()

    fun getTotalBurnedCaloriesStatistics() = database.runsDao().getTotalBurnedCaloriesStatistics()
}
