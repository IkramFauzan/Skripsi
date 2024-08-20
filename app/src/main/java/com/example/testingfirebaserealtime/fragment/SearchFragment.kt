package com.example.testingfirebaserealtime.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.adapter.SearchAdapter
import com.example.testingfirebaserealtime.databinding.FragmentSearchBinding
import com.example.testingfirebaserealtime.detail.DetailAllLocationAcitivity
import com.google.firebase.database.*

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SearchAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var dataList: ArrayList<DataClass>
    private var eventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataList = ArrayList()

        adapter = SearchAdapter(dataList)
        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResultsRecyclerView.adapter = adapter

        adapter.setOnItemClickCallback(object : SearchAdapter.OnItemClickCallback {
            override fun onItemClick(dataClass: DataClass) {
                val intent = Intent(requireContext(), DetailAllLocationAcitivity::class.java).apply {
                    putExtra(DetailAllLocationAcitivity.PLACE, dataClass.placeName)
                }
                startActivity(intent)
            }
        })

        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("allDestination")

        fetchDataFromFirebase()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchList(it) }
                return true
            }
        })
    }

    private fun fetchDataFromFirebase() {
        eventListener = myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataList.clear()
                for (itemSnapshot in snapshot.children) {
                    val dataClass = itemSnapshot.getValue(DataClass::class.java)
                    dataClass?.let { dataList.add(it) }
                }
                adapter.setItemList(dataList)
                Log.d("SearchFragment", "Data loaded: ${dataList.size} items")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error searching", Toast.LENGTH_SHORT).show()
                Log.e("SearchFragment", "Error searching: ${error.message}")
            }
        })
    }

    private fun searchList(text: String) {
        val searchList = ArrayList<DataClass>()
        for (dataClass in dataList) {
            if (dataClass.placeName?.contains(text, ignoreCase = true) == true) {
                searchList.add(dataClass)
            }
        }
        adapter.setItemList(searchList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        eventListener?.let { myRef.removeEventListener(it) }
        _binding = null
    }
}

