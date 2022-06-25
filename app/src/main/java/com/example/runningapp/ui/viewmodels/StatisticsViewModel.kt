package com.example.runningapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.runningapp.models.Run
import com.example.runningapp.repositories.RunsRepository
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    private val repository: RunsRepository
) : ViewModel() {

    fun getTotalDistanceStatistics() = repository.getTotalDistanceStatistics()

    fun getTotalRunTimeStatistics() = repository.getTotalRunTimeStatistics()

    fun getTotalAvgSpeedStatistics() = repository.getTotalAvgSpeedStatistics()

    fun getTotalBurnedCaloriesStatistics() = repository.getTotalBurnedCaloriesStatistics()

    fun getAllRunsSortedByDate(): LiveData<List<Run>> =
        repository.getAllRunsSortedByDate()
}