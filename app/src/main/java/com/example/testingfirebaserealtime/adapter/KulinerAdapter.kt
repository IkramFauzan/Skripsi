package com.example.testingfirebaserealtime.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.databinding.ItemBinding
import com.example.testingfirebaserealtime.detail.DetailKulinerActivity

class KulinerAdapter(private var kulinerList: List<DataClass>) :
    RecyclerView.Adapter<KulinerAdapter.ViwHolder>() {

    private var onItemClick: ((DataClass) -> Unit)? = null

    fun setOnItemClickListener(listener: (DataClass) -> Unit) {
        onItemClick = listener
    }

    fun setItemList(list: List<DataClass>) {
        kulinerList = list
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
                    val item = kulinerList[position]
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
                val intent = Intent(itemView.context, DetailKulinerActivity::class.java)
                Log.d("DetailKulinerActivity", "ID: ${itemList.id}")
                intent.putExtra(DetailKulinerActivity.PLACE_ID, itemList.id)
                intent.putExtra(DetailKulinerActivity.ADDRESS, itemList.placeAddress)
                intent.putExtra(DetailKulinerActivity.PHOTO, itemList.placePhotoUrl)
                intent.putExtra(DetailKulinerActivity.DESC, itemList.description)
                intent.putExtra(DetailKulinerActivity.PLACE, itemList.placeName)
                intent.putExtra(DetailKulinerActivity.LATITUDE, itemList.lat)
                intent.putExtra(DetailKulinerActivity.LONGITUDE, itemList.lon)
                intent.putExtra(DetailKulinerActivity.OPERASIONAL, itemList.operational)
                intent.putExtra(DetailKulinerActivity.FASILITAS, itemList.fasilitas)
                intent.putExtra(DetailKulinerActivity.VIDEO, itemList.placeVideoUrl)
                itemView.context.startActivity(intent)
            }
        }

    }

    override fun onBindViewHolder(holder: ViwHolder, position: Int) {
        val itemList = kulinerList[position]
        holder.bind(itemList)
    }

    override fun getItemCount(): Int = kulinerList.size

}


