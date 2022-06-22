package com.example.runningapp.utilities

import android.view.View
import com.google.android.material.snackbar.Snackbar

object UserInfoCheckingUtility {

    fun checkFilledUserInfo(rootView: View, userName: String, userWeight: String): Boolean {
        return if (areFieldsIncorrect(userName, userWeight)) {
            showIncorrectFieldsMessageSnackbar(rootView)
            false
        } else {
            true
        }
    }

    private fun showIncorrectFieldsMessageSnackbar(view: View) {
        Snackbar.make(
            view,
            "Fill all the fields please",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun areFieldsIncorrect(name: String, weight: String): Boolean =
        name.isEmpty() || weight.isEmpty()
}