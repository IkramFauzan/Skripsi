package com.example.testingfirebaserealtime.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.databinding.ActivityDetailEventBinding

class DetailEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailEventBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailEventBinding.inflate(layoutInflater)

        val imageEvent = intent.getStringExtra(IMAGE)
        val nameEvent = intent.getStringExtra(NAME)
        val dateEvent = intent.getStringExtra(DATE)
        val locEvent = intent.getStringExtra(LOC)
        val detailEvent = intent.getStringExtra(DETAIL)

        binding.apply {
            eventTitle.text = nameEvent
            eventDateTime.text = dateEvent
            eventLocation.text = locEvent
            eventDescription.text = detailEvent
        }

        Glide.with(this)
            .load(imageEvent)
            .into(binding.eventImage)

        setContentView(binding.root)
    }

    companion object {
        const val IMAGE = "image"
        const val NAME = "name"
        const val DETAIL = "detail"
        const val DATE = "date"
        const val LOC = "adrress"
    }
}