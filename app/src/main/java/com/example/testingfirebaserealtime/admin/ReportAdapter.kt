package com.example.testingfirebaserealtime.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.R
import com.example.testingfirebaserealtime.Report
import com.example.testingfirebaserealtime.databinding.ItemReportBinding

class ReportAdapter(
    private val reportList: List<Report>,
    private val onCheckedChange: (Report, Boolean) -> Unit
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reportList[position]
        with(holder.binding) {
            placeNameTextView.text = report.placeName
            timestampTextView.text = report.timestamp
            reportReasonTextView.text = report.reason
            checkbox.isChecked = false  // Default unchecked
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange(report, isChecked)
            }
        }
    }

    override fun getItemCount() = reportList.size
}

