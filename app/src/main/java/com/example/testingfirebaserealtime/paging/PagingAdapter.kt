package com.example.testingfirebaserealtime.paging

import androidx.paging.PagingDataAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.R
import com.example.testingfirebaserealtime.databinding.SearchLayoutBinding

class PagingAdapter : PagingDataAdapter<DataClass, PagingAdapter.DataClassViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DataClass>() {
            override fun areItemsTheSame(oldItem: DataClass, newItem: DataClass): Boolean {
                return oldItem.placeName == newItem.placeName
            }

            override fun areContentsTheSame(oldItem: DataClass, newItem: DataClass): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataClassViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_layout, parent, false)
        return DataClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: DataClassViewHolder, position: Int) {
        val dataClass = getItem(position)
        dataClass?.let { holder.bind(it) }
    }

    class DataClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = SearchLayoutBinding.bind(itemView)

        fun bind(dataClass: DataClass) {
            binding.namaTempatRv.text = dataClass.placeName
            // Bind other views here
        }
    }
}