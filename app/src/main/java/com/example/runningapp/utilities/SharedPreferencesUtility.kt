package com.example.runningapp.utilities

import android.content.SharedPreferences

object SharedPreferencesUtility {

    fun writeUserInfoToSharedPref(sharedPref: SharedPreferences, name: String, weight: Float) {
        sharedPref.edit()
            .putString(Constants.SHARED_PREFERENCES_USER_NAME_KEY, name)
            .putFloat(Constants.SHARED_PREFERENCES_USER_WEIGHT_KEY, weight.toFloat())
            .putBoolean(Constants.SHARED_PREFERENCES_IS_FIRST_APP_LAUNCH_KEY, false)
            .apply()
    }
}