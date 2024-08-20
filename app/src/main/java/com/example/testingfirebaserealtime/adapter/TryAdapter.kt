package com.example.testingfirebaserealtime.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.UlasanCoba
import com.example.testingfirebaserealtime.activity.ProfileActivity
import com.example.testingfirebaserealtime.databinding.ItemRatingBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class TryAdapter(
    private val context: Context,
    private val userDataList: List<UlasanCoba>)
    : RecyclerView.Adapter<TryAdapter.ViewHolder>() {

    private val ratingList: MutableList<Float> = mutableListOf()
    private val komentarList: MutableList<String> = mutableListOf()
    private val usernameList: MutableList<String> = mutableListOf()
    private val dayList: MutableList<String> = mutableListOf()
    private val profilList: MutableList<String> = mutableListOf()
    private val emailList: MutableList<String> = mutableListOf()

    inner class ViewHolder(private val binding: ItemRatingBinding) : RecyclerView.ViewHolder(binding.root) {
        private var email: String? = null

        fun bind(userId: UlasanCoba) {
            if (position >= 0 && position < ratingList.size) {

                binding.ratingTextView.text = userId.rating.toString()
                binding.komentarTextView.text = userId.komentar
                binding.date.text = userId.day
                binding.username.text = userId.name
                Glide.with(binding.root)
                    .load(userId.profile)
                    .circleCrop()
                    .into(binding.profilUsername)


                val auth = Firebase.auth
                val firebaseUser = auth.currentUser


                binding.profilUsername.setOnClickListener {
                    // Mendapatkan referensi ke child "rating" dengan UID pengguna
                    val ratingRef = FirebaseDatabase.getInstance().getReference("rating")

                    // Menambahkan listener untuk mendapatkan data pengguna dari "rating"
                    ratingRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (placeSnapshot in snapshot.children) {
                                for (ratingSnapshot in placeSnapshot.children) {
                                    if (snapshot.exists()) {
                                        // Jika ada, ambil username, profile, dan email dari snapshot
                                        val uid = snapshot.child("uid").value.toString()
                                        val username = snapshot.child("username").value.toString()
                                        val profile = snapshot.child("profileImageUrl").value.toString()
                                        val email = snapshot.child("email").value.toString()

                                        // Buat Intent untuk memulai ProfileActivity dan kirimkan data pengguna
                                        val intent = Intent(context, ProfileActivity::class.java).apply {
                                            //putExtra("username", username)
                                            putExtra("email", email)
                                            putExtra("profile", profile)
                                            putExtra("uid", uid)
                                        }
                                        // Memulai ProfileActivity
                                        context.startActivity(intent)
                                    } else {
                                        // Jika data pengguna tidak ditemukan, tampilkan pesan kesalahan
                                        Toast.makeText(context, "User data not found in rating", Toast.LENGTH_SHORT).show()
                                        Log.e("MyAdapter", "User data not found in rating")
                                    }
                                }
                            }

                            // Memeriksa apakah data pengguna ada
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Jika terjadi kesalahan dalam mengambil data, tampilkan pesan kesalahan
                            Toast.makeText(context, "Failed to fetch user data: ${error.message}", Toast.LENGTH_SHORT).show()
                            Log.e("MyAdapter", "Failed to fetch user data: ${error.message}")
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
        holder.bind(userDataList[position])
    }

    override fun getItemCount(): Int {
        return minOf(
            ratingList.size,
            komentarList.size,
            usernameList.size,
            dayList.size,
            profilList.size,
            emailList.size
        )
    }

    fun addRating(
        rating: List<Float>,
        komentar: List<String>,
        username: List<String>,
        day: List<String>,
        profile: List<String>,
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

}