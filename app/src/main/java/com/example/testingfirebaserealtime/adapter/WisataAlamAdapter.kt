package com.example.testingfirebaserealtime.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.databinding.ItemBinding
import com.example.testingfirebaserealtime.detail.DetailWisataActivity

class WisataAlamAdapter(private var wisataListData: List<DataClass>) :
    RecyclerView.Adapter<WisataAlamAdapter.WisataViwHolder>() {

    //private var wisataList: List<Wisata> = emptyList()
    private var onItemClick: ((DataClass) -> Unit)? = null

    fun setOnItemClickListener(listener: (DataClass) -> Unit) {
        onItemClick = listener
    }

    fun setWisataList(list: List<DataClass>) {
        wisataListData = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WisataViwHolder {
        val binding = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WisataViwHolder(binding)
    }

    inner class WisataViwHolder(private val binding: ItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val wisata = wisataListData[position]
                    onItemClick?.invoke(wisata)
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
                val intent = Intent(itemView.context, DetailWisataActivity::class.java)
                intent.putExtra(DetailWisataActivity.ADDRESS, itemList.placeAddress)
                intent.putExtra(DetailWisataActivity.PHOTO, itemList.placePhotoUrl)
                intent.putExtra(DetailWisataActivity.DESC, itemList.description)
                intent.putExtra(DetailWisataActivity.PLACE, itemList.placeName)
                intent.putExtra(DetailWisataActivity.LATITUDE, itemList.lat)
                intent.putExtra(DetailWisataActivity.LONGITUDE, itemList.lon)
                itemView.context.startActivity(intent)
            }
        }

    }

    override fun onBindViewHolder(holder: WisataViwHolder, position: Int) {
        val wisataListData = wisataListData[position]
        holder.bind(wisataListData)
    }

    override fun getItemCount(): Int = wisataListData.size

}