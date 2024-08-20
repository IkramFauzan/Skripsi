package com.example.testingfirebaserealtime.admin

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.testingfirebaserealtime.R
import com.example.testingfirebaserealtime.databinding.ActivitySelectLocationBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class SelectLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivitySelectLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnSelectLocation.setOnClickListener {
            val selectedLocation = mMap.cameraPosition.target
            val intent = Intent().apply {
                putExtra(UpdateDataActivity.LATITUDE, selectedLocation)
                putExtra(UpdateDataActivity.LONGITUDE, selectedLocation)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        // Tambahkan marker di lokasi default (misal: Jakarta) dan geser kamera
        val defaultLocation = LatLng(-5.310289, 119.742604)
        mMap.addMarker(MarkerOptions().position(defaultLocation).title("Default Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12.0f))

        // Set a marker on map click and return the selected location
        mMap.setOnMapClickListener { latLng ->
            mMap.clear() // clear previous markers
            mMap.addMarker(MarkerOptions().position(latLng))
        }
    }
}