package com.example.testingfirebaserealtime.eksperimen

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.admin.UpdateDataActivity
import com.example.testingfirebaserealtime.databinding.ItemBinding
import com.example.testingfirebaserealtime.databinding.ItemListWisataAdminBinding
import com.example.testingfirebaserealtime.detail.DetailKulinerActivity

class ListWisataAdapter(
    private val listener: OnDeleteClickListener,
    private var kulinerList: List<DataClass>
    ) : RecyclerView.Adapter<ListWisataAdapter.ViwHolder>() {

    interface OnDeleteClickListener {
        fun onDeleteClicked(tempat: DataClass)
    }

    fun setItemList(list: List<DataClass>) {
        kulinerList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViwHolder {
        val binding = ItemListWisataAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViwHolder(binding)
    }

    inner class ViwHolder(private val binding: ItemListWisataAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(itemList: DataClass) {
            val place = itemList.placeName
            binding.namaTempatRv.text = place
            Glide.with(itemView.context)
                .load(itemList.placePhotoUrl)
                .into(binding.imageTempatRv)
            binding.deleteButton.setOnClickListener {
                listener.onDeleteClicked(itemList)
            }
            binding.editButton.setOnClickListener {
                val intent = Intent(itemView.context, UpdateDataActivity::class.java).apply {
                    putExtra(UpdateDataActivity.PLACE, itemList.placeName)
                    putExtra(UpdateDataActivity.ADDRESS, itemList.placeAddress)
                    putExtra(UpdateDataActivity.DESC, itemList.description)
                    putExtra(UpdateDataActivity.PHOTO, itemList.placePhotoUrl)
                    putExtra(UpdateDataActivity.LATITUDE, itemList.lat)
                    putExtra(UpdateDataActivity.LONGITUDE, itemList.lon)
                    putExtra(UpdateDataActivity.OPERASIONAL, itemList.operational)
                    putExtra(UpdateDataActivity.FASILITAS, itemList.fasilitas)
                }
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