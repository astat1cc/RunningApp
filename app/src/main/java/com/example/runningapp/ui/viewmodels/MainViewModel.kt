package com.example.runningapp.ui.viewmodels

import androidx.lifecycle.*
import com.example.runningapp.models.Run
import com.example.runningapp.repositories.RunsRepository
import com.example.runningapp.utilities.Constants
import com.example.runningapp.utilities.Constants.RUNS_SORTED_BY_AVG_SPEED_INDEX
import com.example.runningapp.utilities.Constants.RUNS_SORTED_BY_BURNED_CALORIES_INDEX
import com.example.runningapp.utilities.Constants.RUNS_SORTED_BY_DATE_INDEX
import com.example.runningapp.utilities.Constants.RUNS_SORTED_BY_DISTANCE_INDEX
import com.example.runningapp.utilities.Constants.RUNS_SORTED_BY_RUN_TIME_INDEX
import com.example.runningapp.utilities.SortType
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

class MainViewModel(
    private val repository: RunsRepository
) : ViewModel() {

    val runs = MediatorLiveData<List<Run>>()

    private lateinit var runsSortedByDate: LiveData<List<Run>>

    private val allDifferentlySortedRuns: List<LiveData<List<Run>>>

    var runsSortType = MutableLiveData(Constants.DEFAULT_SORT_TYPE)

    init {
        allDifferentlySortedRuns = (0..4).map { i ->
            when (i) {
                RUNS_SORTED_BY_DATE_INDEX -> getAllRunsSortedByDate()
                RUNS_SORTED_BY_DISTANCE_INDEX -> getAllRunsSortedByDistance()
                RUNS_SORTED_BY_RUN_TIME_INDEX -> getAllRunsSortedByRunTime()
                RUNS_SORTED_BY_AVG_SPEED_INDEX -> getAllRunsSortedByAvgSpeed()
                RUNS_SORTED_BY_BURNED_CALORIES_INDEX -> getAllRunsSortedByBurnedCalories()
                else -> throw Exception("Undefined index of sorted runs")
            }
        }
        runs.addAllSources()
    }

    private fun MediatorLiveData<List<Run>>.addAllSources() {
        allDifferentlySortedRuns.forEachIndexed { i, sortedRuns ->
            runs.addSource(sortedRuns) { sortedRunsValue ->
                if (runsSortType.value?.index == allDifferentlySortedRuns.indexOf(sortedRuns)) {
                    runs.postValue(sortedRunsValue)
                }
            }
        }
    }

    fun sortRunsBy(sortType: SortType) {
        val sortTypeIndex = sortType.index
        allDifferentlySortedRuns[sortTypeIndex].value?.let { sortedRuns ->
            runs.postValue(sortedRuns)
        }
    }

    fun saveRun(run: Run) {
        viewModelScope.launch {
            repository.saveRun(run)
        }
    }

    private fun getAllRunsSortedByDate(): LiveData<List<Run>> =
        repository.getAllRunsSortedByDate()

    private fun getAllRunsSortedByDistance(): LiveData<List<Run>> =
        repository.getAllRunsSortedByDistance()

    private fun getAllRunsSortedByRunTime(): LiveData<List<Run>> =
        repository.getAllRunsSortedByRunTime()

    private fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>> =
        repository.getAllRunsSortedByAvgSpeed()

    private fun getAllRunsSortedByBurnedCalories(): LiveData<List<Run>> =
        repository.getAllRunsSortedByBurnedCalories()


    class Factory @Inject constructor(
        private val repository: RunsRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MainViewModel(repository) as T
    }
}