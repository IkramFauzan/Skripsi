package com.example.testingfirebaserealtime.admin.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.R
import com.example.testingfirebaserealtime.Report
import com.example.testingfirebaserealtime.adapter.ListAddDestinationAdapter
import com.example.testingfirebaserealtime.admin.ReportAdapter
import com.example.testingfirebaserealtime.databinding.FragmentHomeBinding
import com.example.testingfirebaserealtime.databinding.FragmentListAddDestinationBinding
import com.google.firebase.database.*

class ListAddDestinationFragment : Fragment() {

    private lateinit var binding: FragmentListAddDestinationBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ListAddDestinationAdapter

    private lateinit var reportList: MutableList<Report>
    private val selectedReports = mutableListOf<Report>()
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReport: DatabaseReference
    private lateinit var adapterReport: ReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListAddDestinationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().reference.child("temporary")
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
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

        reportList = mutableListOf()
        adapterReport = ReportAdapter(reportList) { report, isChecked ->
            if (isChecked) {
                selectedReports.add(report)
            } else {
                selectedReports.remove(report)
            }
        }

        binding.recyclerViewLaporan.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewLaporan.adapter = adapterReport

        databaseReport = FirebaseDatabase.getInstance().getReference("reports")

        databaseReport.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reportList.clear()
                for (dataSnapshot in snapshot.children) {
                    val report = dataSnapshot.getValue(Report::class.java)
                    report?.let { reportList.add(it) }
                }
                adapterReport.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        binding.deleteButton.setOnClickListener {
            deleteSelectedReports()
        }
    }

    private fun deleteSelectedReports() {
        for (report in selectedReports) {
            val placeName = report.placeName
            if (placeName.isNotEmpty()) {
                val query = databaseReport.orderByChild("placeName").equalTo(placeName)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (dataSnapshot in snapshot.children) {
                            dataSnapshot.ref.removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Report deleted successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Failed to delete report", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Failed to delete report: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(requireContext(), "Place name is invalid", Toast.LENGTH_SHORT).show()
            }
        }
        selectedReports.clear()
    }

}