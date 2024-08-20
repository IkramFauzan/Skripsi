package com.example.testingfirebaserealtime.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testingfirebaserealtime.adapter.ProfileUlasanAdapter
import com.example.testingfirebaserealtime.databinding.ActivityProfileUlasanBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileUlasanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileUlasanBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ProfileUlasanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileUlasanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        val ref = database.getReference("rating")

        binding.recyclerViewUlasan.layoutManager = LinearLayoutManager(this)
        adapter = ProfileUlasanAdapter()
        binding.recyclerViewUlasan.adapter = adapter

        val username = intent.getStringExtra("username")
        Log.d("ProfileUlasanActivity", "Username: $ref")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val ratingList = mutableListOf<Float>()
                val komentarList = mutableListOf<String>()
                val dayList = mutableListOf<String>()
                val usernameList = mutableListOf<String>()
                val profilList = mutableListOf<String>()
                val placeList = mutableListOf<String>()

                for (placeSnapshot in snapshot.children) {
                    for (ratingSnapshot in placeSnapshot.children) {
                        // Mengambil data ulasan yang sesuai dengan username
                        if (ratingSnapshot.child("name").getValue(String::class.java) == username) {
                            val placeName = placeSnapshot.key
                            Log.d("ProfileUlasanActivity", "Place: $placeName")
                            val rating = ratingSnapshot.child("rating").getValue(Float::class.java)
                            val komentar = ratingSnapshot.child("komentar").getValue(String::class.java)
                            val day = ratingSnapshot.child("day").getValue(String::class.java)
                            val username = ratingSnapshot.child("name").getValue(String::class.java)
                            val profile = ratingSnapshot.child("profile").getValue(String::class.java)

                            // Tambahkan data ke dalam list
                            placeName?.let { placeList.add(it) }
                            rating?.let { ratingList.add(it) }
                            komentar?.let { komentarList.add(it) }
                            day?.let { dayList.add(it) }
                            username?.let { usernameList.add(it) }
                            profile?.let { profilList.add(it) }
                        }
                    }
                }
                // Setelah mendapatkan semua data, update adapter
                adapter.addRating(ratingList, komentarList, usernameList, dayList, profilList, placeList)
                Log.d("ProfileUlasanActivity", "Adapter: $adapter")
            }

            override fun onCancelled(error: DatabaseError) {
                // Tangani kesalahan
                Log.e("ProfileUlasanActivity", "Database error: ${error.message}")
            }
        })
    }
}