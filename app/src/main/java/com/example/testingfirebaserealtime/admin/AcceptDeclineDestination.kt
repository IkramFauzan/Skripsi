package com.example.testingfirebaserealtime.admin

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.MediaController
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.R
import com.example.testingfirebaserealtime.databinding.ActivityAcceptDeclineDestinationBinding
import com.example.testingfirebaserealtime.databinding.ItemImageBinding
import com.example.testingfirebaserealtime.eksperimen.MediaPlayerActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

@Suppress("DEPRECATION")
class AcceptDeclineDestination : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityAcceptDeclineDestinationBinding
    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private var photoUri: Uri? = null
    private var videoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAcceptDeclineDestinationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val address = intent.getStringExtra(ADDRESS)
        val name = intent.getStringExtra(NAME)
        val category = intent.getStringExtra(CATEGORY)
        val photo = intent.getStringExtra(PHOTO)
        val opera = intent.getStringExtra(HOURS)
        val fasilitas = intent.getStringExtra(FACILITIES)
        val detaails = intent.getStringExtra(DETAILS)
        val videoUrl = intent.getStringExtra(VIDEO)

        binding.apply {
            etDetails.text = detaails
            etAddress.text = address
            etName.text = name
            etHours.text = opera
            etFacilities.text = fasilitas
            spinnerCategory.text = category
        }

        Glide.with(this)
            .load(photo)
            .into(binding.ivPhoto)

        binding.lihatVideo.setOnClickListener {
            val intent = Intent(this, MediaPlayerActivity::class.java)
            intent.putExtra(MediaPlayerActivity.VIDEO, videoUrl)
            startActivity(intent)
        }

        if (detaails != null && detaails.length > 50) {
            binding.tampilkanLebihBanyakButton.visibility = View.VISIBLE
            var isExpanded = false
            binding.tampilkanLebihBanyakButton.setOnClickListener {
                if (!isExpanded) {
                    binding.etDetails.maxLines = Integer.MAX_VALUE
                    binding.tampilkanLebihBanyakButton.text = "Tampilkan Lebih Sedikit"
                } else {
                    binding.etDetails.maxLines = 2
                    binding.tampilkanLebihBanyakButton.text = "Tampilkan Lebih Banyak"
                }
                isExpanded = !isExpanded
            }
        }

        if (opera != null) {
            binding.showMore.visibility = View.VISIBLE
            var isExpanded = false
            binding.showMore.setOnClickListener {
                if (!isExpanded) {
                    binding.etHours.maxLines = Integer.MAX_VALUE
                    binding.showMore.text = "Tampilkan Lebih Sedikit"
                } else {
                    binding.etHours.maxLines = 2
                    binding.showMore.text = "Tampilkan Lebih Banyak"
                }
                isExpanded = !isExpanded
            }
        }

        if (fasilitas != null) {
            binding.showMore2.visibility = View.VISIBLE
            var isExpanded = false
            binding.showMore2.setOnClickListener {
                if (!isExpanded) {
                    binding.etFacilities.maxLines = Integer.MAX_VALUE
                    binding.showMore2.text = "Tampilkan Lebih Sedikit"
                } else {
                    binding.etFacilities.maxLines = 2
                    binding.showMore2.text = "Tampilkan Lebih Banyak"
                }
                isExpanded = !isExpanded
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        database = FirebaseDatabase.getInstance().reference

        binding.btnSave.setOnClickListener {
            saveDestination()
        }

        binding.btnDelete.setOnClickListener {
            deleteFromTemporary()
        }
    }
    private fun saveDestination() {
        val name = binding.etName.text.toString()
        val address = binding.etAddress.text.toString()
        val details = binding.etDetails.text.toString()
        val hours = binding.etHours.text.toString()
        val facilities = binding.etFacilities.text.toString()
        val category = binding.spinnerCategory.text.toString()
        val photo = intent.getStringExtra(PHOTO)
        val lat = intent.getDoubleExtra(LAT, 0.0)
        val lon = intent.getDoubleExtra(LON, 0.0)
        val video = intent.getStringExtra(VIDEO)

        val database = FirebaseDatabase.getInstance().reference
        val categoryRef = database.child("tempat").child(category)

        val anotherChildRef = database.child("allDestination")

        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val childrenKeys = snapshot.children.mapNotNull { it.key?.toIntOrNull() }
                val newKey = if (childrenKeys.isNotEmpty()) {
                    (childrenKeys.maxOrNull() ?: 0) + 1
                } else {
                    0
                }.toString()

                val destination = photo?.let {
                    DataClass(
                        placeAddress = address,
                        placeName = name,
                        placePhotoUrl = it,
                        description = details,
                        lat = lat,
                        lon = lon,
                        operational = hours,
                        fasilitas = facilities,
                        category = category,
                        placeVideoUrl = video.toString(),
                        id = newKey.toInt()
                    )
                }

                val destinationRef = categoryRef.child(newKey)

                val anotherChildDestinationRef = anotherChildRef.child(newKey)

                val storageRef = FirebaseStorage.getInstance().reference
                val photoRef = storageRef.child("photos/$newKey.jpg")

                photoUri?.let { uri ->
                    photoRef.putFile(uri)
                        .addOnSuccessListener { uploadTask ->
                            uploadTask.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                                val destinationWithPhotoUrl = destination?.copy(placePhotoUrl = downloadUri.toString())
                                destinationRef.setValue(destinationWithPhotoUrl)
                                    .addOnSuccessListener {
                                        Toast.makeText(this@AcceptDeclineDestination, "Destinasi disimpan", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this@AcceptDeclineDestination, "Gagal menyimpan destinasi", Toast.LENGTH_SHORT).show()
                                    }
                                anotherChildDestinationRef.setValue(destinationWithPhotoUrl)
                                    .addOnSuccessListener {
                                    }
                                    .addOnFailureListener {
                                    }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@AcceptDeclineDestination, "Gagal mengunggah foto", Toast.LENGTH_SHORT).show()
                        }
                } ?: run {
                    destinationRef.setValue(destination)
                        .addOnSuccessListener {
                            anotherChildDestinationRef.setValue(destination)
                                .addOnSuccessListener {
                                    // Data saved successfully, now delete from "temporary"
                                    deleteFromTemporary()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this@AcceptDeclineDestination,
                                        "Gagal menyimpan destinasi ke 'semua'",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@AcceptDeclineDestination,
                                "Gagal menyimpan destinasi ke 'tempat'",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AcceptDeclineDestination, "Gagal mendapatkan data kategori", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        val address = intent.getStringExtra(ADDRESS)
        val place = intent.getStringExtra(NAME)
        val lat = intent.getDoubleExtra(LAT, 0.0)
        val lon = intent.getDoubleExtra(LON, 0.0)

        val latLng = LatLng(lat, lon)
        mMap.addMarker(MarkerOptions().position(latLng).title(place).snippet(address))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
    }

    private fun deleteFromTemporary() {
        val tempRef = database.child("temporary")
        tempRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Data temporary dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menghapus data temporary", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        const val NAME = "name"
        const val ADDRESS = "address"
        const val DETAILS = "details"
        const val HOURS = "hours"
        const val FACILITIES = "facilities"
        const val CATEGORY = "category"
        const val PHOTO = "photo"
        const val LAT = "location"
        const val LON = "lon"
        const val VIDEO = "video"
    }
}
