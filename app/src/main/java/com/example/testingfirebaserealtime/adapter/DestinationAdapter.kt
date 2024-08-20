package com.example.testingfirebaserealtime.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.databinding.ItemDestinationBinding
import com.example.testingfirebaserealtime.penginapan.activity.DetailHotelActivity

class DestinationAdapter(private var destinations: List<DataClass>
) : RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder>() {

    private var onItemClick: ((DataClass) -> Unit)? = null

    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickListener(listener: (DataClass) -> Unit) {
        onItemClick = listener
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClick(dataClass: DataClass)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val binding = ItemDestinationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DestinationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        val destination = destinations[position]
        holder.bind(destination)
    }

    override fun getItemCount(): Int {
        return destinations.size
    }

    fun setData(destinations: List<DataClass>) {
        this.destinations = destinations
        notifyDataSetChanged()
    }

    fun setItemList(list: List<DataClass>) {
        destinations = list
        notifyDataSetChanged()
    }

    inner class DestinationViewHolder(private val binding: ItemDestinationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(destination: DataClass) {
            binding.root.setOnClickListener {
                onItemClickCallback.onItemClick(destination)
            }
            binding.textViewDestinationName.text = destination.placeName
            binding.textViewDestinationCategory.text = destination.placeAddress
            Glide.with(itemView.context)
                .load(destination.placePhotoUrl)
                .into(binding.imageViewDestination)
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailHotelActivity::class.java)
                intent.putExtra(DetailHotelActivity.ID, destination.id)
                intent.putExtra(DetailHotelActivity.ADDRESS, destination.placeAddress)
                intent.putExtra(DetailHotelActivity.PHOTO, destination.placePhotoUrl)
                intent.putExtra(DetailHotelActivity.DESC, destination.description)
                intent.putExtra(DetailHotelActivity.PLACE, destination.placeName)
                intent.putExtra(DetailHotelActivity.LATITUDE, destination.lat)
                intent.putExtra(DetailHotelActivity.LONGITUDE, destination.lon)
                //intent.putExtra(DetailHotelActivity.KOMENTAR, destination.komentar)
                itemView.context.startActivity(intent)
            }
        }
    }
}
