package com.example.runningapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.appComponent
import com.example.runningapp.databinding.FragmentSetupBinding
import com.example.runningapp.utilities.SharedPreferencesUtility
import com.example.runningapp.utilities.UserInfoCheckingUtility
import javax.inject.Inject

class SetupFragment : Fragment() {

    private lateinit var binding: FragmentSetupBinding

    @Inject
    lateinit var sharedPref: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appComponent.inject(this)

        binding.tvContinue.setOnClickListener {
            tvContinuePressed()
        }
    }

    private fun tvContinuePressed() {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        val areFieldsCorrect =
            UserInfoCheckingUtility.checkFilledUserInfo(binding.root, name, weight)
        if (areFieldsCorrect) {
            SharedPreferencesUtility.writeUserInfoToSharedPref(sharedPref, name, weight.toFloat())
            findNavController().navigate(R.id.action_setupFragment_to_runsFragment)
        }
    }
}