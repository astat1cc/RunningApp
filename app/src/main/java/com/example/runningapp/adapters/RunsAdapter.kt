package com.example.runningapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningapp.databinding.ItemRunBinding
import com.example.runningapp.models.Run
import com.example.runningapp.utilities.TrackingUtility
import java.text.SimpleDateFormat
import java.util.*

class RunsAdapter : RecyclerView.Adapter<RunsAdapter.RunViewHolder>() {

    class RunViewHolder(val binding: ItemRunBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean =
            oldItem.hashCode() == newItem.hashCode()
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) {
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRunBinding.inflate(inflater, parent, false)
        return RunViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.binding.apply {
            Glide.with(this.root).load(run.image).into(ivRunImage)
            tvAvgSpeed.text = "${run.avgSpeedInKPH} km/h"
            tvCalories.text = "${run.burnedCalories} kcal"
            tvDistance.text = if (run.distanceInMeters >= 1000) {
                "${run.distanceInMeters / 1000.0} km"
            } else {
                "${run.distanceInMeters} m"
            }
            tvTime.text = TrackingUtility.getStopWatchFormatFromMillis(run.runTimeInMillis)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            tvDate.text = dateFormat.format(calendar.time)
        }
    }

    override fun getItemCount(): Int =
        differ.currentList.size
}