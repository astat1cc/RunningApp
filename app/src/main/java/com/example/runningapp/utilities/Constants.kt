package com.example.runningapp.utilities

import android.graphics.Color

object Constants {

    const val ACTION_START_TRACKING_SERVICE = "ACTION_START_TRACKING_SERVICE"
    const val ACTION_RESUME_TRACKING_SERVICE = "ACTION_RESUME_TRACKING_SERVICE"
    const val ACTION_PAUSE_TRACKING_SERVICE = "ACTION_PAUSE_TRACKING_SERVICE"
    const val ACTION_FINISH_TRACKING_SERVICE = "ACTION_FINISH_TRACKING_SERVICE"
    const val ACTION_CANCEL_RUN = "ACTION_CANCEL_RUN"
    const val ACTION_TRACKING_SERVICE_NOTIFICATION_PRESSED = "ACTION_TRACKING_SERVICE_NOTIFICATION_PRESSED"

    const val TRACKING_SERVICE_NOTIFICATION_CHANNEL_ID = "TRACKING_SERVICE_NOTIFICATION_CHANNEL_ID"
    const val TRACKING_SERVICE_NOTIFICATION_CHANNEL_NAME = "TRACKING_SERVICE_NOTIFICATION_CHANNEL_NAME"
    const val TRACKING_SERVICE_NOTIFICATION_NAME = "TRACKING_SERVICE_NOTIFICATION_NAME"
    const val TRACKING_SERVICE_NOTIFICATION_ID = 1

    const val MAP_POLYLINE_COLOR = Color.RED
    const val MAP_POLYLINE_WIDTH = 7f
    const val MAP_CAMERA_ZOOM = 17f

    const val LOCATION_TRACKING_REQUEST_INTERVAL = 4000L
    const val LOCATION_TRACKING_REQUEST_FASTEST_INTERVAL = 1500L
    const val TIMER_UPDATE_INTERVAL = 75L

    val DEFAULT_SORT_TYPE = SortType.DATE

    const val RUNS_SORTED_BY_DATE_INDEX = 0
    const val RUNS_SORTED_BY_DISTANCE_INDEX = 1
    const val RUNS_SORTED_BY_RUN_TIME_INDEX = 2
    const val RUNS_SORTED_BY_AVG_SPEED_INDEX = 3
    const val RUNS_SORTED_BY_BURNED_CALORIES_INDEX = 4

    const val SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES_NAME"
    const val SHARED_PREFERENCES_USER_NAME_KEY = "SHARED_PREFERENCES_USER_NAME_KEY"
    const val SHARED_PREFERENCES_USER_WEIGHT_KEY = "SHARED_PREFERENCES_USER_WEIGHT"
    const val SHARED_PREFERENCES_IS_FIRST_APP_LAUNCH_KEY = "SHARED_PREFERENCES_IS_FIRST_APP_LAUNCH"

    const val BAR_CHART_AXES_AND_TEXT_COLOR = Color.WHITE

    const val CANCEL_TRACKING_DIALOG_TAG = "CANCEL_TRACKING_DIALOG_TAG"

}