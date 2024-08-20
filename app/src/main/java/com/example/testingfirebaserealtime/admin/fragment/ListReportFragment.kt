package com.example.testingfirebaserealtime.admin.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.testingfirebaserealtime.MainActivity
import com.example.testingfirebaserealtime.R
import com.example.testingfirebaserealtime.databinding.FragmentListReportBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class ListReportFragment : Fragment() {

    private lateinit var binding: FragmentListReportBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.toMain.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        db = FirebaseDatabase.getInstance()

        setupDonutChart()
        loadDataForDonutChart()
        fetchTotalSubmissions()
    }


    private fun setupDonutChart() {
        val pieChart: PieChart = binding.pieChart

        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.setDrawCenterText(true)
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true

        pieChart.setDrawEntryLabels(false)
        pieChart.description.isEnabled = false

        pieChart.legend.isEnabled = false
    }

    private fun loadDataForDonutChart() {
        val categories = listOf("wisataBuatan", "kuliner", "penginapan", "wisataSejarah", "wisataAlam")
        val dataMap = mutableMapOf<String, Int>()

        val dbRef = FirebaseDatabase.getInstance().reference.child("tempat")

        for (category in categories) {
            dbRef.child(category).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dataMap[category] = snapshot.childrenCount.toInt()
                    if (dataMap.size == categories.size) {
                        updateDonutChart(dataMap)
                        updateLegend(dataMap)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateDonutChart(dataMap: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()
        for ((category, count) in dataMap) {
            entries.add(PieEntry(count.toFloat(), category))
        }

        val dataSet = PieDataSet(entries, "Categories")
        dataSet.colors = listOf(
            Color.rgb(60, 170, 230), // Blue
            Color.rgb(255, 103, 96), // Red
            Color.rgb(144, 202, 144), // Green
            Color.rgb(255, 235, 59), // Yellow
            Color.rgb(0, 188, 212), // Cyan
            Color.rgb(233, 30, 99) // Magenta
        )

        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        val data = PieData(dataSet)
        data.setDrawValues(false)

        binding.pieChart.data = data
        binding.pieChart.invalidate()
    }

    private fun updateLegend(dataMap: Map<String, Int>) {
        val context = context ?: return // Pastikan context tidak null
        val legendLayout: LinearLayout = binding.legendLayout
        legendLayout.removeAllViews()

        val colors = listOf(
            Color.rgb(60, 170, 230), // Blue
            Color.rgb(255, 103, 96), // Red
            Color.rgb(144, 202, 144), // Green
            Color.rgb(255, 235, 59), // Yellow
            Color.rgb(0, 188, 212), // Cyan
            Color.rgb(233, 30, 99) // Magenta
        )

        var colorIndex = 0
        val paddingBetween = 16 // Padding antara warna dan teks
        val circleSize = 40 // Ukuran lingkaran warna
        val paddingTop = 8 // Padding atas

        for ((category, count) in dataMap) {
            val legendItem = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(8, paddingTop, 8, 8) // Padding: left, top, right, bottom
                gravity = Gravity.CENTER_VERTICAL
            }

            val colorView = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(circleSize, circleSize).apply {
                    setMargins(0, 0, paddingBetween, 0)
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(colors[colorIndex % colors.size])
                }
            }

            val textView = TextView(context).apply {
                text = category.split(" ").joinToString(" ") { it.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                } }
                setTextColor(Color.BLACK)
                textSize = 16f
                typeface = resources.getFont(R.font.poppins_medium)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val countView = TextView(context).apply {
                text = "$count"
                setTextColor(Color.BLACK)
                textSize = 16f
                setPadding(paddingBetween, 0, paddingBetween, 0)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.END
                }
            }

            legendItem.addView(colorView)
            legendItem.addView(textView)
            legendItem.addView(countView)

            legendLayout.addView(legendItem)

            colorIndex++
        }
    }


    private fun fetchTotalSubmissions() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("temporary")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalSubmissions = snapshot.childrenCount
                binding.totalSubmissions.text = "Total Ajuan Wisata User: $totalSubmissions"
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if necessary
            }
        })
    }
}
