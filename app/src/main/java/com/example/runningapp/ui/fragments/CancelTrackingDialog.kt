package com.example.runningapp.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.runningapp.R

class CancelTrackingDialog : DialogFragment() {

    private var positiveButtonListener: (() -> Unit)? = null

    fun setPositiveButtonListener(listener: (() -> Unit)?) {
        positiveButtonListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage("Are you sure to cancel the current run and delete all its data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Delete") { _, _ ->
                positiveButtonListener?.let {
                    it()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
}