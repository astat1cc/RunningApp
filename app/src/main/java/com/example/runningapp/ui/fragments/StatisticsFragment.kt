package com.example.runningapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.runningapp.R
import com.example.runningapp.appComponent
import com.example.runningapp.databinding.FragmentStatisticsBinding
import com.example.runningapp.ui.viewmodels.StatisticsViewModel
import com.example.runningapp.utilities.Constants.BAR_CHART_AXES_AND_TEXT_COLOR
import com.example.runningapp.utilities.TrackingUtility
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import javax.inject.Inject

class StatisticsFragment : Fragment() {

    private lateinit var binding: FragmentStatisticsBinding

    @Inject
    lateinit var viewModelFactory: StatisticsViewModel.Factory
    private val viewModel: StatisticsViewModel by viewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appComponent.inject(this)

        subscribeToObservers()

        setupBarChart()
    }

    private fun setupBarChart() {
        val barChart = binding.barChart
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            setDrawGridLines(false)
            axisLineColor = BAR_CHART_AXES_AND_TEXT_COLOR
            textColor = BAR_CHART_AXES_AND_TEXT_COLOR
        }
        barChart.axisLeft.apply {
            axisLineColor = BAR_CHART_AXES_AND_TEXT_COLOR
            textColor = BAR_CHART_AXES_AND_TEXT_COLOR
            setDrawGridLines(false)
        }
        barChart.axisRight.apply {
            axisLineColor = BAR_CHART_AXES_AND_TEXT_COLOR
            textColor = BAR_CHART_AXES_AND_TEXT_COLOR
            setDrawGridLines(false)
        }
        barChart.apply {
            description.text = ""
        }
    }

    private fun subscribeToObservers() {
        with(viewModel) {
            getTotalDistanceStatistics().observe(viewLifecycleOwner) {
                it?.let { binding.tvTotalDistance.text = it.toString() }
            }
            getTotalRunTimeStatistics().observe(viewLifecycleOwner) {
                it?.let {
                    binding.tvTotalTime.text = TrackingUtility.getStopWatchFormatFromMillis(it)
                }
            }
            getTotalAvgSpeedStatistics().observe(viewLifecycleOwner) {
                it?.let {
                    binding.tvAverageSpeed.text = it.toString()
                }
            }
            getTotalBurnedCaloriesStatistics().observe(viewLifecycleOwner) {
                it?.let {
                    binding.tvTotalCalories.text = it.toString()
                }
            }
            getAllRunsSortedByDate().observe(viewLifecycleOwner) { runs ->
                val allAvgSpeeds = runs.indices.map { i ->
                    BarEntry(i.toFloat(), runs[i].avgSpeedInKPH.toFloat())
                }
                val barDataSet = BarDataSet(allAvgSpeeds, "Average speed").apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                }
                binding.barChart.data = BarData(barDataSet)
                binding.barChart.invalidate()
            }
        }
    }
}