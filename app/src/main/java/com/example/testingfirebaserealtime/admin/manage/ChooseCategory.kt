package com.example.testingfirebaserealtime.admin.manage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.testingfirebaserealtime.penginapan.activity.HotelActivity
import com.example.testingfirebaserealtime.activity.KulinerActivity
import com.example.testingfirebaserealtime.activity.PrayerActivity
import com.example.testingfirebaserealtime.activity.WisataAlamActivity
import com.example.testingfirebaserealtime.category.WisataBuatanActivity
import com.example.testingfirebaserealtime.category.SejarahActivity
import com.example.testingfirebaserealtime.databinding.ActivityChooseCategoryBinding
import com.example.testingfirebaserealtime.eksperimen.ListWisataAdapter

class ChooseCategory : AppCompatActivity() {

    private lateinit var binding: ActivityChooseCategoryBinding
    private lateinit var adapter: ListWisataAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChooseCategoryBinding.inflate(layoutInflater)

        binding.nature.setOnClickListener {
            val intent = Intent(this, HotelActivity::class.java)
            startActivity(intent)
        }

        binding.shopping.setOnClickListener {
            val intent = Intent(this, KulinerActivity::class.java)
            startActivity(intent)
        }

        binding.history.setOnClickListener {
            val intent = Intent(this, PrayerActivity::class.java)
            startActivity(intent)
        }

        binding.culture.setOnClickListener {
            val intent = Intent(this, WisataAlamActivity::class.java)
            startActivity(intent)
        }

        binding.food.setOnClickListener {
            val intent = Intent(this, WisataBuatanActivity::class.java)
            startActivity(intent)
        }

        binding.adventure.setOnClickListener {
            val intent = Intent(this, SejarahActivity::class.java)
            startActivity(intent)
        }

        setContentView(binding.root)
    }
}