package com.example.testingfirebaserealtime.activity

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testingfirebaserealtime.Visit
import com.example.testingfirebaserealtime.VisitHistoryAdapter
import com.example.testingfirebaserealtime.databinding.ActivityVisitBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class VisitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVisitBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var visitList: MutableList<Visit>
    private lateinit var visitAdapter: VisitHistoryAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var geocoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewVisits.layoutManager = LinearLayoutManager(this)
        visitList = mutableListOf()
        visitAdapter = VisitHistoryAdapter(visitList)
        binding.recyclerViewVisits.adapter = visitAdapter

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())

        // Record visit when activity starts
        recordVisit()
        // Display visit history
        displayVisitHistory()
    }

    private fun recordVisit() {
        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Get current location
            fusedLocationClient.lastLocation
                .addOnSuccessListener(this) { location ->
                    if (location != null) {
                        // Save visit data to Firebase Realtime Database
                        saveVisitData(location.latitude, location.longitude)
                    }
                }
        } else {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun saveVisitData(latitude: Double, longitude: Double) {
        // Save visit data to Firebase Realtime Database
        val userId = auth.currentUser?.uid
        val visitsRef = database.getReference("visits")

        userId?.let {
            val visitData = HashMap<String, Any>()
            visitData["latitude"] = latitude
            visitData["longitude"] = longitude
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            visitData["date"] = currentDate

            // Check if the visit for the current date already exists
            val query = visitsRef.child(it).orderByChild("date").equalTo(currentDate)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Jika sudah ada kunjungan pada tanggal yang sama, batalkan penyimpanan data baru
                        Log.d("VisitActivity", "Visit for today already exists")
                    } else {
                        // Jika belum ada kunjungan pada tanggal yang sama, simpan data kunjungan baru
                        visitsRef.child(it).push().setValue(visitData)
                            .addOnSuccessListener {
                                Log.d("VisitActivity", "Visit data saved successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("VisitActivity", "Error saving visit data", e)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("VisitActivity", "Error checking existing visits", error.toException())
                }
            })
        }
    }


    private fun displayVisitHistory() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        val visitsRef = database.getReference("visits")

        userId?.let { uid ->
            visitsRef.child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    visitList.clear()
                    for (visitSnapshot in snapshot.children) {
                        val latitude = visitSnapshot.child("latitude").getValue(Double::class.java)
                        val longitude = visitSnapshot.child("longitude").getValue(Double::class.java)
                        val day = visitSnapshot.child("date").getValue(String::class.java)
                        val address = getAddress(latitude ?: 0.0, longitude ?: 0.0)
                        val visit = Visit(latitude ?: 0.0,
                            longitude ?: 0.0,
                            address ?: "",
                            day ?: "")
                        visitList.add(visit)
                    }
                    visitAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun getAddress(latitude: Double, longitude: Double): String? {
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    return address.getAddressLine(0)
                }
            }
        } catch (e: IOException) {
            Log.e("VisitActivity", "Error getting address", e)
        }
        return null
    }

    private fun timestampToDate(timestamp: Long): String? {
        try {
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
            val netDate = Date(timestamp)
            return sdf.format(netDate)
        } catch (e: Exception) {
            Log.e("VisitActivity", "Error converting timestamp to date", e)
        }
        return null
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 123
    }
}
