package com.example.testingfirebaserealtime

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VisitHistoryAdapter(private val visitList: List<Visit>) : RecyclerView.Adapter<VisitHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val locationNameTextView: TextView = itemView.findViewById(R.id.textViewLocationName)
        val locationTextView: TextView = itemView.findViewById(R.id.textViewLocation)
        val timestampTextView: TextView = itemView.findViewById(R.id.textViewTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_visit_history, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentVisit = visitList[position]

        holder.locationNameTextView.text = "Location Name: ${currentVisit.locationName}"
        holder.locationTextView.text = "Location: ${currentVisit.latitude}, ${currentVisit.longitude}"
        holder.timestampTextView.text = "Timestamp: ${currentVisit.timestamp}"
    }

    override fun getItemCount() = visitList.size
}