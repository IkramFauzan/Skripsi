package com.example.testingfirebaserealtime.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.adapter.WisataAlamAdapter
import com.example.testingfirebaserealtime.databinding.ActivityWisataBinding
import com.google.firebase.database.*

class WisataAlamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWisataBinding
    private lateinit var adapter: WisataAlamAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWisataBinding.inflate(layoutInflater)

        database = FirebaseDatabase.getInstance()
        myRef = FirebaseDatabase.getInstance().reference

        val wisataRef = myRef.child("tempat").child("wisataAlam")

        adapter = WisataAlamAdapter(emptyList())
        binding.usersrecyclerview.layoutManager = LinearLayoutManager(this)
        binding.usersrecyclerview.adapter = adapter

        binding.progressBar.visibility = View.VISIBLE

        wisataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val list = mutableListOf<DataClass>()
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val tempatWisata = snapshot.getValue(DataClass::class.java)
                        tempatWisata?.let { list.add(it) }
                    }
                    adapter.setWisataList(list)
                }

                binding.progressBar.visibility = View.GONE

            }

            override fun onCancelled(error: DatabaseError) {
                print("Kenapa salah woi")
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        setContentView(binding.root)
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    companion object {
        private const val TAG = "PrayerActivity"
    }

}