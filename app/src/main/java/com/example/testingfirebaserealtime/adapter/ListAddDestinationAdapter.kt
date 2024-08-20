package com.example.testingfirebaserealtime.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.admin.AcceptDeclineDestination
import com.example.testingfirebaserealtime.databinding.ItemAddDestinationBinding

class ListAddDestinationAdapter(
    private var destinations: List<DataClass>,
) : RecyclerView.Adapter<ListAddDestinationAdapter.DestinationViewHolder>() {

    private var onItemClick: ((DataClass) -> Unit)? = null

    fun setOnItemClickListener(listener: (DataClass) -> Unit) {
        onItemClick = listener
    }

    fun setItemList(list: List<DataClass>) {
        destinations = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val binding = ItemAddDestinationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DestinationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        val itemList = destinations[position]
        holder.bind(itemList)
    }

    override fun getItemCount() = destinations.size

    inner class DestinationViewHolder(private val binding: ItemAddDestinationBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = destinations[position]
                    onItemClick?.invoke(item)
                }
            }
        }
        fun bind(destination: DataClass) {
            binding.namaTempatRv.text = destination.placeName

            // Load photo if available
            destination.placePhotoUrl.let { imageUrl ->
                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .into(binding.imageTempatRv)
            }

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, AcceptDeclineDestination::class.java)
                intent.putExtra(AcceptDeclineDestination.NAME, destination.placeName)
                intent.putExtra(AcceptDeclineDestination.ADDRESS, destination.placeAddress)
                intent.putExtra(AcceptDeclineDestination.DETAILS, destination.description)
                intent.putExtra(AcceptDeclineDestination.HOURS, destination.operational)
                intent.putExtra(AcceptDeclineDestination.FACILITIES, destination.fasilitas)
                intent.putExtra(AcceptDeclineDestination.CATEGORY, destination.category)
                intent.putExtra(AcceptDeclineDestination.LAT, destination.lat)
                intent.putExtra(AcceptDeclineDestination.LON, destination.lon)
                intent.putExtra(AcceptDeclineDestination.VIDEO, destination.placeVideoUrl)

                // Check if photo is available before adding it to the intent
                destination.placePhotoUrl?.let { imageUrl ->
                    intent.putExtra(AcceptDeclineDestination.PHOTO, imageUrl)
                }

                itemView.context.startActivity(intent)
            }
        }

    }
}