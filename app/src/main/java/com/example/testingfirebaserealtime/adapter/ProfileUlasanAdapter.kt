package com.example.testingfirebaserealtime.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.R

class ProfileUlasanAdapter(
    private val ratingList: MutableList<Float> = mutableListOf(),
    private val komentarList: MutableList<String> = mutableListOf(),
    private val usernameList: MutableList<String> = mutableListOf(),
    private val dayList: MutableList<String> = mutableListOf(),
    private val profilList: MutableList<String> = mutableListOf(),
    private val tempatList: MutableList<String> = mutableListOf()
) : RecyclerView.Adapter<ProfileUlasanAdapter.ViewHolder>() {

    private var data = ArrayList<DataClass>()
    private var onItemClick: ((DataClass) -> Unit)? = null

    fun setOnItemClickListener(listener: (DataClass) -> Unit) {
        onItemClick = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.username)
        val ratingTextView: TextView = itemView.findViewById(R.id.ratingTextView)
        val komentarTextView: TextView = itemView.findViewById(R.id.komentarTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.date)
        val profilImageView: ImageView = itemView.findViewById(R.id.profilUsername)
        val tempatTextView: TextView = itemView.findViewById(R.id.tempat)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ulasan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("KyuuAdapter", "Position: $position")
        val rating = ratingList[position]
        val komentar = komentarList[position]
        val username = usernameList[position]
        val day = dayList[position]
        val profile = profilList[position]
        val tempat = tempatList[position]

        holder.ratingTextView.text = rating.toString()
        holder.komentarTextView.text = komentar
        holder.dateTextView.text = day
        holder.usernameTextView.text = username
        holder.tempatTextView.text = tempat
        Glide.with(holder.itemView.context)
            .load(profile)
            .circleCrop()
            .into(holder.profilImageView)


    }


    override fun getItemCount(): Int {
        return minOf(ratingList.size,
            komentarList.size,
            usernameList.size,
            dayList.size,
            profilList.size,
            tempatList.size)
    }

    fun addRating(
        rating: List<Float>,
        komentar: List<String>,
        username: List<String>,
        day: List<String>,
        profile: List<String>,
        tempat: List<String>
    ) {
        ratingList.clear()
        ratingList.addAll(rating)
        komentarList.clear()
        komentarList.addAll(komentar)
        usernameList.clear()
        usernameList.addAll(username)
        dayList.clear()
        dayList.addAll(day)
        profilList.clear()
        profilList.addAll(profile)
        tempatList.clear()
        tempatList.addAll(tempat)
        notifyDataSetChanged()
    }
}