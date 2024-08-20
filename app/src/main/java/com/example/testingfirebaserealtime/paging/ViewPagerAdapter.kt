package com.example.testingfirebaserealtime.paging

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.databinding.SearchLayoutBinding
import com.example.testingfirebaserealtime.penginapan.activity.DetailHotelActivity

class ViewPagerAdapter(private var placeList: List<DataClass>) : RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder>() {

    private var onItemClick: ((DataClass) -> Unit)? = null

    fun setOnItemClickListener(listener: (DataClass) -> Unit) {
        onItemClick = listener
    }

    fun setItemList(list: List<DataClass>) {
        placeList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        val view = SearchLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewPagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        holder.bind(placeList[position])
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

    inner class ViewPagerViewHolder(private val binding: SearchLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = placeList[position]
                    onItemClick?.invoke(item)
                }
            }
        }

        fun bind(wisata: DataClass) {
            binding.apply {
                namaTempatRv.text = wisata.placeName
                Glide.with(itemView.context)
                    .load(wisata.placePhotoUrl)
                    .into(binding.imageTempatRv)
                itemView.setOnClickListener {
                    val intent = Intent(itemView.context, DetailHotelActivity::class.java)
                    intent.putExtra(DetailHotelActivity.ID, wisata.id)
                    intent.putExtra(DetailHotelActivity.ADDRESS, wisata.placeAddress)
                    intent.putExtra(DetailHotelActivity.PHOTO, wisata.placePhotoUrl)
                    intent.putExtra(DetailHotelActivity.DESC, wisata.description)
                    intent.putExtra(DetailHotelActivity.PLACE, wisata.placeName)
                    intent.putExtra(DetailHotelActivity.LATITUDE, wisata.lat)
                    intent.putExtra(DetailHotelActivity.LONGITUDE, wisata.lon)
                    itemView.context.startActivity(intent)
                }
            }
        }
    }
}