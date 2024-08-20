package com.example.testingfirebaserealtime.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var profileImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        val username = intent.getStringExtra("username")
        val email = intent.getStringExtra("email")
        val profile = intent.getStringExtra("profile")
        val uid = intent.getStringExtra("uid")

        binding.emailTextView.text = email
        binding.usernameTextView.text = username
        Glide.with(this)
            .load(profile)
            .circleCrop()
            .into(binding.profileImageView)

        uid?.let {
            binding.userUlasan.setOnClickListener {
                val intent = Intent(this@ProfileActivity, ProfileUlasanActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
            }
            binding.userFavorite.setOnClickListener {
                val intent = Intent(this, ListFavoriteActivity::class.java)
                intent.putExtra("uid", uid)
                //intent.putExtra("username", username)
                startActivity(intent)
            }
        }

        binding.visit.setOnClickListener {
            val intent = Intent(this, VisitActivity::class.java)
            startActivity(intent)
        }

    }

}
