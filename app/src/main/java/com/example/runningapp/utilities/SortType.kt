package com.example.runningapp.utilities

import com.example.runningapp.utilities.Constants.RUNS_SORTED_BY_AVG_SPEED_INDEX
import com.example.runningapp.utilities.Constants.RUNS_SORTED_BY_BURNED_CALORIES_INDEX
import com.example.runningapp.utilities.Constants.RUNS_SORTED_BY_DATE_INDEX
import com.example.runningapp.utilities.Constants.RUNS_SORTED_BY_DISTANCE_INDEX
import com.example.runningapp.utilities.Constants.RUNS_SORTED_BY_RUN_TIME_INDEX

enum class SortType(val index: Int) {
    DATE(RUNS_SORTED_BY_DATE_INDEX),
    DISTANCE(RUNS_SORTED_BY_DISTANCE_INDEX),
    RUN_TIME(RUNS_SORTED_BY_RUN_TIME_INDEX),
    AVG_SPEED(RUNS_SORTED_BY_AVG_SPEED_INDEX),
    BURNED_CALORIES(RUNS_SORTED_BY_BURNED_CALORIES_INDEX)
}