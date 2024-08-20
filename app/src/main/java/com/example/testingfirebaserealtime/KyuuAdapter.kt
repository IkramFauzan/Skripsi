package com.example.testingfirebaserealtime

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.activity.ProfileActivity
import com.example.testingfirebaserealtime.databinding.ItemRatingBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class KyuuAdapter(
    private val context: Context
) : RecyclerView.Adapter<KyuuAdapter.ViewHolder>() {

    private val ratingList: MutableList<Float> = mutableListOf()
    private val komentarList: MutableList<String> = mutableListOf()
    private val usernameList: MutableList<String> = mutableListOf()
    private val dayList: MutableList<String> = mutableListOf()
    private val profilList: MutableList<String> = mutableListOf()
    private val usersMap: MutableMap<String, User> = mutableMapOf()

    private val ulasan = ArrayList<User>()

    inner class ViewHolder(private val binding: ItemRatingBinding) : RecyclerView.ViewHolder(binding.root) {
        private var email: String? = null // Deklarasikan properti email di sini

        fun bind(position: Int) {
            if (position >= 0 && position < minOf(ratingList.size, komentarList.size, usernameList.size, dayList.size, profilList.size)) {
                val rating = ratingList[position]
                val komentar = komentarList[position]
                val username = usernameList[position]
                val day = dayList[position]
                val profile = profilList[position]

                binding.ratingTextView.text = rating.toString()
                binding.komentarTextView.text = komentar
                binding.date.text = day
                binding.username.text = username
                Glide.with(binding.root)
                    .load(profile)
                    .circleCrop()
                    .into(binding.profilUsername)

                binding.profilUsername.setOnClickListener {
                    val databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    databaseReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (userSnapshot in snapshot.children) {
                                    val uid = userSnapshot.key // Mendapatkan UID pengguna
                                    val email = userSnapshot.child("email").getValue(String::class.java) // Mendapatkan email pengguna

                                    // Membuat dan mengirim Intent dengan username dan email
                                    val intent = Intent(context, ProfileActivity::class.java)
                                    intent.putExtra("username", username)
                                    intent.putExtra("email", email)
                                    intent.putExtra("profile", profile)
                                    intent.putExtra("uid", uid)
                                    context.startActivity(intent)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRatingBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return minOf(
            ratingList.size,
            komentarList.size,
            usernameList.size,
            dayList.size,
            profilList.size
        )
    }

    fun addRating(
        rating: List<Float>,
        komentar: List<String>,
        username: List<String>,
        day: List<String>,
        profile: List<String>
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
        notifyDataSetChanged()
    }

    fun setUsersMap(usersMap: Map<String, User>) {
        this.usersMap.clear()
        this.usersMap.putAll(usersMap)
        notifyDataSetChanged()
    }
}

