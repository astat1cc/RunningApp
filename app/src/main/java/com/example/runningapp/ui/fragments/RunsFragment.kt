package com.example.runningapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runningapp.R
import com.example.runningapp.adapters.RunsAdapter
import com.example.runningapp.appComponent
import com.example.runningapp.databinding.FragmentRunsBinding
import com.example.runningapp.ui.viewmodels.MainViewModel
import com.example.runningapp.utilities.Constants
import com.example.runningapp.utilities.SortType
import javax.inject.Inject

class RunsFragment : Fragment() {

    private lateinit var binding: FragmentRunsBinding

    @Inject
    lateinit var sharedPref: SharedPreferences

    private val isFirstAppLaunch: Boolean
        get() = sharedPref.getBoolean(Constants.SHARED_PREFERENCES_IS_FIRST_APP_LAUNCH_KEY, true)

    private lateinit var runsAdapter: RunsAdapter

    @Inject
    lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRunsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appComponent.inject(this)

        if (isFirstAppLaunch) {
            val navOptions =
                NavOptions.Builder().setPopUpTo(R.id.setupFragment, true, true).build()
            findNavController().navigate(
                R.id.setupFragment,
                savedInstanceState,
                navOptions
            )
        }

        setupRecyclerView()

        with(binding.spinnerFilter) {
            onItemSelectedListener = createOnItemSelectedListener()
        }

        subscribeToObservers()

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runsFragment_to_trackingFragment)
        }
    }

    private fun createOnItemSelectedListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            adapterView: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            val sortType = SortType.values()[position]
            viewModel.runsSortType.postValue(sortType)
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {}
    }

    private fun subscribeToObservers() {
        with(viewModel) {
            runs.observe(viewLifecycleOwner) { list ->
                runsAdapter.submitList(list)
            }

            runsSortType.observe(viewLifecycleOwner) { sortType ->
                sortRunsBy(sortType)
            }
        }
    }

    private fun setupRecyclerView() {
        runsAdapter = RunsAdapter()
        binding.rvRuns.apply {
            adapter = runsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
}