package com.example.testingfirebaserealtime.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.databinding.ItemBinding
import com.example.testingfirebaserealtime.detail.DetailPrayerActivity

class PrayerAdapter(private var hotelList: List<DataClass>) :
    RecyclerView.Adapter<PrayerAdapter.ViwHolder>() {

    private var onItemClick: ((DataClass) -> Unit)? = null

    fun setOnItemClickListener(listener: (DataClass) -> Unit) {
        onItemClick = listener
    }

    fun setItemList(list: List<DataClass>) {
        hotelList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViwHolder {
        val binding = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViwHolder(binding)
    }

    inner class ViwHolder(private val binding: ItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = hotelList[position]
                    onItemClick?.invoke(item)
                }
            }
        }

        fun bind(itemList: DataClass) {
            val address = itemList.placeAddress
            val place = itemList.placeName
            binding.tempatWisata.text = place
            binding.alamatWisata.text = address
            Glide.with(itemView.context)
                .load(itemList.placePhotoUrl)
                .into(binding.wisataImage)
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailPrayerActivity::class.java)
                intent.putExtra(DetailPrayerActivity.ADDRESS, itemList.placeAddress)
                intent.putExtra(DetailPrayerActivity.PHOTO, itemList.placePhotoUrl)
                intent.putExtra(DetailPrayerActivity.DESC, itemList.description)
                intent.putExtra(DetailPrayerActivity.PLACE, itemList.placeName)
                intent.putExtra(DetailPrayerActivity.LATITUDE, itemList.lat)
                intent.putExtra(DetailPrayerActivity.LONGITUDE, itemList.lon)
                itemView.context.startActivity(intent)
            }
        }

    }

    override fun onBindViewHolder(holder: ViwHolder, position: Int) {
        val itemList = hotelList[position]
        holder.bind(itemList)
    }

    override fun getItemCount(): Int = hotelList.size

}