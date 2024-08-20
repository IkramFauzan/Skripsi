package com.example.testingfirebaserealtime.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.R
import com.example.testingfirebaserealtime.databinding.ActivityAddDestinationBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddDestinationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityAddDestinationBinding
    private var photoUri: Uri? = null
    private var videoUri: Uri? = null
    private var coordinates: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDestinationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Spinner dengan adapter dan pilihan kategori
        ArrayAdapter.createFromResource(
            this,
            R.array.category_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
        }

        binding.btnPickLocation.setOnClickListener {
            val mapIntent = Intent(this, MapPickerActivity::class.java)
            startActivityForResult(mapIntent, MAP_PICKER_REQUEST_CODE)
        }

        binding.btnPickPhoto.setOnClickListener {
            pickImageFromGallery()
        }

        binding.btnPickVideo.setOnClickListener {
            pickVideoFromGallery()
        }

        binding.btnSave.setOnClickListener {
            saveDestination()
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE)
    }

    private fun pickVideoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        startActivityForResult(intent, VIDEO_PICKER_REQUEST_CODE)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // This method is required for OnMapReadyCallback but not used here
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MAP_PICKER_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    coordinates = data?.getParcelableExtra("coordinates")
                    coordinates?.let {
                        binding.tvCoordinates.text = "Coordinates: ${it.latitude}, ${it.longitude}"
                    }
                }
            }
            IMAGE_PICKER_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    photoUri = data?.data
                    binding.ivPhoto.setImageURI(photoUri)
                }
            }
            VIDEO_PICKER_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    videoUri = data?.data
                    // Tampilkan preview video atau lakukan yang lainnya
                }
            }
        }
    }

    private fun saveDestination() {
        val name = binding.etName.text.toString()
        val address = binding.etAddress.text.toString()
        val details = binding.etDetails.text.toString()
        val hours = binding.etHours.text.toString()
        val facilities = binding.etFacilities.text.toString()
        val category = binding.spinnerCategory.selectedItem.toString()
        val coords = coordinates?.let { "${it.latitude}, ${it.longitude}" } ?: ""
        val lat = coordinates?.latitude ?: 0.0
        val lon = coordinates?.longitude ?: 0.0

        if (name.isEmpty() || address.isEmpty() || details.isEmpty() || hours.isEmpty() || facilities.isEmpty() || coords.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        showProgressBar()

        val database = FirebaseDatabase.getInstance().reference
        val categoryRef = database.child("temporary").push()

        val destination = DataClass(
            placeAddress = address,
            placeName = name,
            placePhotoUrl = "",
            description = details,
            lat = lat,
            lon = lon,
            operational = hours,
            fasilitas = facilities,
            category = category,
            placeVideoUrl = "",
            id = 0 // ID can be set to 0 because we are using the push ID as the unique key
        )

        val storageRef = FirebaseStorage.getInstance().reference
        val photoRef = storageRef.child("photos/${categoryRef.key}.jpg")

        photoUri?.let { uri ->
            photoRef.putFile(uri)
                .addOnSuccessListener { uploadTask ->
                    uploadTask.storage.downloadUrl.addOnSuccessListener { photoDownloadUri ->
                        val destinationWithPhotoUrl = destination.copy(placePhotoUrl = photoDownloadUri.toString())
                        saveVideo(destinationWithPhotoUrl, categoryRef)
                    }
                }
                .addOnFailureListener {
                    hideProgressBar()
                    Toast.makeText(this, "Gagal mengunggah foto", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            saveVideo(destination, categoryRef)
        }
    }

    private fun saveVideo(destination: DataClass, categoryRef: DatabaseReference) {
        val storageRef = FirebaseStorage.getInstance().reference
        val videoRef = storageRef.child("videos/${categoryRef.key}.mp4")

        videoUri?.let { uri ->
            videoRef.putFile(uri)
                .addOnSuccessListener { uploadTask ->
                    uploadTask.storage.downloadUrl.addOnSuccessListener { videoDownloadUri ->
                        val destinationWithVideoUrl = destination.copy(placeVideoUrl = videoDownloadUri.toString())
                        saveDestinationToDatabase(destinationWithVideoUrl, categoryRef)
                    }
                }
                .addOnFailureListener {
                    hideProgressBar()
                    Toast.makeText(this, "Gagal mengunggah video", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            saveDestinationToDatabase(destination, categoryRef)
        }
    }

    private fun saveDestinationToDatabase(destination: DataClass, categoryRef: DatabaseReference) {
        categoryRef.setValue(destination)
            .addOnSuccessListener {
                hideProgressBar()
                Toast.makeText(this, "Destinasi disimpan", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                hideProgressBar()
                Toast.makeText(this, "Gagal menyimpan destinasi", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val MAP_PICKER_REQUEST_CODE = 1
        private const val IMAGE_PICKER_REQUEST_CODE = 2
        private const val VIDEO_PICKER_REQUEST_CODE = 3
    }
}

