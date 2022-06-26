package com.example.runningapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.runningapp.models.Run
import com.example.runningapp.repositories.RunsRepository
import javax.inject.Inject

class StatisticsViewModel(
    private val repository: RunsRepository
) : ViewModel() {

    fun getTotalDistanceStatistics() = repository.getTotalDistanceStatistics()

    fun getTotalRunTimeStatistics() = repository.getTotalRunTimeStatistics()

    fun getTotalAvgSpeedStatistics() = repository.getTotalAvgSpeedStatistics()

    fun getTotalBurnedCaloriesStatistics() = repository.getTotalBurnedCaloriesStatistics()

    fun getAllRunsSortedByDate(): LiveData<List<Run>> =
        repository.getAllRunsSortedByDate()


    class Factory @Inject constructor(
        private val repository: RunsRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StatisticsViewModel(repository) as T
    }
}