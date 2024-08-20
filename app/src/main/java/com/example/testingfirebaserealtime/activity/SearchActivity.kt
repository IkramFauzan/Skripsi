package com.example.testingfirebaserealtime.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.adapter.SearchAdapter
import com.example.testingfirebaserealtime.databinding.ActivitySearchBinding
import com.example.testingfirebaserealtime.detail.DetailAllLocationAcitivity
import com.google.firebase.database.*

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: SearchAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var dataList: ArrayList<DataClass>
    var eventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchBinding.inflate(layoutInflater)

        dataList = ArrayList()

        adapter = SearchAdapter(emptyList())
        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchResultsRecyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("allDestination")

        eventListener = myRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataList.clear()
                for (itemSnapshot in snapshot.children) {
                    val dataClass = itemSnapshot.getValue(DataClass::class.java)
                    if (dataClass != null) {
                        dataList.add(dataClass)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Error searching: ${error.message}")
                Toast.makeText(this@SearchActivity, "Error searching", Toast.LENGTH_SHORT).show()
            }
        })
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                p0?.let {
                    searchList(it)
                }
                return true
            }
        })

        adapter.setOnItemClickCallback(object : SearchAdapter.OnItemClickCallback{
            override fun onItemClick(dataClass: DataClass) {
                val intent = Intent(this@SearchActivity, DetailAllLocationAcitivity::class.java).putExtra(
                    DetailAllLocationAcitivity.DATA, dataClass)
                startActivity(intent)
            }
        })

        setContentView(binding.root)
    }

    fun searchList(text: String) {
        val searchList = ArrayList<DataClass>()
        for (dataClass in dataList) {
            if (dataClass.placeName?.lowercase()?.contains(text.lowercase()) == true) {
                searchList.add(dataClass)
            }
        }
        adapter.setItemList(searchList)
    }

}