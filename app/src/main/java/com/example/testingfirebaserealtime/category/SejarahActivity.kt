package com.example.testingfirebaserealtime.category

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.penginapan.adapter.HotelAdapter
import com.example.testingfirebaserealtime.databinding.ActivitySejarahBinding
import com.google.firebase.database.*

class SejarahActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySejarahBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var adapter: HotelAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySejarahBinding.inflate(layoutInflater)

        database = FirebaseDatabase.getInstance()
        //myRef = database.getReference("penginapan")
        myRef = FirebaseDatabase.getInstance().reference
        val hotelRef = myRef.child("tempat").child("wisataSejarah")

        binding.usersrecyclerview.layoutManager = LinearLayoutManager(this)
        adapter = HotelAdapter(emptyList())
        binding.usersrecyclerview.adapter = adapter

        binding.progressBar.visibility = View.VISIBLE

        hotelRef.addValueEventListener(object : ValueEventListener { //bisa mengembalikan menggunakan myRef karena hotelRef hanya untuk testing pemanggilan child tempat
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val list = mutableListOf<DataClass>()
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val tempatWisata = snapshot.getValue(DataClass::class.java)
                        tempatWisata?.let { list.add(it) }
                    }
                    adapter.setItemList(list)
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

    companion object {
        private const val TAG = "HotelActivity"
    }
}