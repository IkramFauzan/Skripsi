package com.example.testingfirebaserealtime.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testingfirebaserealtime.Favorite
import com.example.testingfirebaserealtime.adapter.ListFavoriteAdapter
import com.example.testingfirebaserealtime.databinding.ActivityListFavoriteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListFavoriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListFavoriteBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ListFavoriteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        val favoritesRef = database.getReference("favorites").child(userId ?: "")

        adapter = ListFavoriteAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        favoritesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favoritesList = mutableListOf<Favorite>()
                for (favoriteSnapshot in snapshot.children) {
                    val placeId = favoriteSnapshot.key // Mengambil placeId sebagai kunci
                    val placeName = favoriteSnapshot.child("placeName").getValue(String::class.java)
                    val placeAddress = favoriteSnapshot.child("placeAddress").getValue(String::class.java)
                    val placeImageUrl = favoriteSnapshot.child("placePhotoUrl").getValue(String::class.java)

                    if (placeId != null && placeName != null && placeAddress != null && placeImageUrl != null) {
                        val favorite = Favorite(placeName, placeAddress, placeImageUrl)
                        favoritesList.add(favorite)
                    }
                }

                if (favoritesList.isEmpty()) {
                    Toast.makeText(this@ListFavoriteActivity, "Tidak ada favorit yang tersedia", Toast.LENGTH_SHORT).show()
                }

                adapter.submitList(favoritesList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ListFavoriteActivity, "Gagal membaca favorit", Toast.LENGTH_SHORT).show()
            }
        })
    }
}


