package com.example.testingfirebaserealtime.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.EventAd
import com.example.testingfirebaserealtime.databinding.ItemEventBinding

class EventAdapter(
    private val eventList: List<EventAd>,
    private val itemClickListener: (EventAd) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(eventAd: EventAd) {
            with(binding) {
                Glide.with(root.context)
                    .load(eventAd.imageUrl)
                    .into(imageEvent)
                textEventName.text = eventAd.eventName
                textEventDescription.text = eventAd.eventDescription

                itemView.setOnClickListener {
                    itemClickListener(eventAd)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEventBinding.inflate(inflater, parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(eventList[position])
    }

    override fun getItemCount() = eventList.size
}
