package com.example.testingfirebaserealtime.penginapan.adapter


import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.databinding.ItemBinding
import com.example.testingfirebaserealtime.penginapan.activity.DetailHotelActivity

class HotelAdapter(private var hotelList: List<DataClass>) :
    RecyclerView.Adapter<HotelAdapter.ViwHolder>() {

    private var ratingList: MutableList<Float> = mutableListOf()

    private var onItemClick: ((DataClass) -> Unit)? = null

    fun setOnItemClickListener(listener: (DataClass) -> Unit) {
        onItemClick = listener
    }

    fun setItemList(list: List<DataClass>) {
        hotelList = list
        notifyDataSetChanged()
    }

    fun addRating(rating: List<Float>) {
        ratingList.clear()
        ratingList.addAll(rating)
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
                val intent = Intent(itemView.context, DetailHotelActivity::class.java)
                intent.putExtra(DetailHotelActivity.ID, itemList.id)
                intent.putExtra(DetailHotelActivity.ADDRESS, itemList.placeAddress)
                intent.putExtra(DetailHotelActivity.PHOTO, itemList.placePhotoUrl)
                intent.putExtra(DetailHotelActivity.DESC, itemList.description)
                intent.putExtra(DetailHotelActivity.PLACE, itemList.placeName)
                intent.putExtra(DetailHotelActivity.LATITUDE, itemList.lat)
                intent.putExtra(DetailHotelActivity.LONGITUDE, itemList.lon)
                //intent.putExtra(DetailHotelActivity.KOMENTAR, itemList.komentar)
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
