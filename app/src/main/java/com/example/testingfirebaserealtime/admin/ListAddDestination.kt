package com.example.testingfirebaserealtime.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.adapter.ListAddDestinationAdapter
import com.example.testingfirebaserealtime.databinding.ActivityListAddDestinationBinding
import com.google.firebase.database.*

class ListAddDestination : AppCompatActivity() {

    private lateinit var binding: ActivityListAddDestinationBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ListAddDestinationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListAddDestinationBinding.inflate(layoutInflater)

        database = FirebaseDatabase.getInstance().reference.child("temporary")
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ListAddDestinationAdapter(emptyList())
        binding.recyclerView.adapter = adapter

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val list = mutableListOf<DataClass>()
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val tempatWisata = snapshot.getValue(DataClass::class.java)
                        tempatWisata?.let { list.add(it) }
                    }
                    adapter.setItemList(list)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                print("Kenapa salah woi")
            }
        })

        setContentView(binding.root)
    }
}