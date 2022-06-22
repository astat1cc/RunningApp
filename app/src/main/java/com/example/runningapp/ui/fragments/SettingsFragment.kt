package com.example.runningapp.ui.fragments

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.example.runningapp.appComponent
import com.example.runningapp.databinding.FragmentSettingsBinding
import com.example.runningapp.utilities.Constants
import com.example.runningapp.utilities.SharedPreferencesUtility
import com.example.runningapp.utilities.UserInfoCheckingUtility
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appComponent.inject(this)

        fillEditTextsWithUserInfo()

        binding.btnApplyChanges.setOnClickListener {
            btnApplyPressed()
        }
    }

    private fun btnApplyPressed() {
        handleFilledUserInfo()
        hideSoftKeyboard()
        fillEditTextsWithUserInfo()
    }

    private fun handleFilledUserInfo() {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        val areFieldsCorrect =
            UserInfoCheckingUtility.checkFilledUserInfo(binding.root, name, weight)
        if (areFieldsCorrect) {
            SharedPreferencesUtility.writeUserInfoToSharedPref(sharedPref, name, weight.toFloat())
            showSuccessChangingSnackbar()
        }
    }

    private fun hideSoftKeyboard() {
        val inputMethodService =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val focusedEditText = requireActivity().currentFocus
        inputMethodService.hideSoftInputFromWindow(focusedEditText?.windowToken, 0)
        with(binding) {
            etName.clearFocus()
            etWeight.clearFocus()
        }
    }

    private fun showSuccessChangingSnackbar() {
        Snackbar.make(binding.root, "Changes successfully applied", LENGTH_SHORT).show()
    }

    private fun fillEditTextsWithUserInfo() {
        with(binding) {
            etName.setText(
                sharedPref.getString(Constants.SHARED_PREFERENCES_USER_NAME_KEY, "") ?: ""
            )
            etWeight.setText(
                sharedPref.getFloat(Constants.SHARED_PREFERENCES_USER_WEIGHT_KEY, 80f).toString()
            )
        }
    }
}