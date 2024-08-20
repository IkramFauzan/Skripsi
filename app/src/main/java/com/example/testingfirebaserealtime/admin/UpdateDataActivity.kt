package com.example.testingfirebaserealtime.admin

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.example.testingfirebaserealtime.R
import com.example.testingfirebaserealtime.databinding.ActivityUpdateDataBinding
import com.example.testingfirebaserealtime.profile.MapPickerActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.textfield.TextInputEditText

class UpdateDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateDataBinding
    private lateinit var database: DatabaseReference
    private val MAP_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("tempat")

        // Ambil data dari Intent
        val placeName = intent.getStringExtra(PLACE)
        val address = intent.getStringExtra(ADDRESS)
        val description = intent.getStringExtra(DESC)
        val operationalTime = intent.getStringExtra(OPERASIONAL)
        val facilities = intent.getStringExtra(FASILITAS)
        val image = intent.getStringExtra(PHOTO)
        val latitude = intent.getDoubleExtra(LATITUDE, 0.0)
        val longitude = intent.getDoubleExtra(LONGITUDE, 0.0)
        Log.d("Update", latitude.toString())
        Log.d("Update", longitude.toString())

        // Isi EditText dengan data yang ada
        binding.editPlaceName.setText(placeName)
        binding.editAddress.setText(address)
        binding.editDescription.setText(description)
        binding.editOperationalTime.setText(operationalTime)
        binding.editFacilities.setText(facilities)
        binding.editLatitude.setText(latitude.toString())
        binding.editLongitude.setText(longitude.toString())
        Glide.with(this)
            .load(image)
            .into(binding.placeImageView)

        // Set listener untuk tombol update
        binding.updateButton.setOnClickListener {
            updatePlace(placeName)
        }

        // Set listener untuk tombol pilih lokasi
        binding.btnSelectLocation.setOnClickListener {
            val intent = Intent(this, SelectLocationActivity::class.java)
            startActivityForResult(intent, MAP_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val latitude = data?.getDoubleExtra(LATITUDE, 0.0)
            val longitude = data?.getDoubleExtra(LONGITUDE, 0.0)
            if (latitude != null && longitude != null) {
                binding.editLatitude.setText(latitude.toString())
                binding.editLongitude.setText(longitude.toString())
            }
        }
    }
    private fun updatePlace(originalPlaceName: String?) {
        val image = intent.getStringExtra(PHOTO)
        val updatedPlace = mapOf(
            "placeName" to binding.editPlaceName.text.toString(),
            "placeAddress" to binding.editAddress.text.toString(),
            "description" to binding.editDescription.text.toString(),
            "placePhotoUrl" to image,
            "operational" to binding.editOperationalTime.text.toString(),
            "fasilitas" to binding.editFacilities.text.toString(),
            "lat" to binding.editLatitude.text.toString().toDoubleOrNull(),
            "lon" to binding.editLongitude.text.toString().toDoubleOrNull()
        )

        binding.progressBar.visibility = View.VISIBLE

        val ref = FirebaseDatabase.getInstance().reference
        val tempatRef = ref.child("tempat")
        val semuaRef = ref.child("allDestination")

        val updateTasks = mutableListOf<Task<Void>>()

        tempatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (node in snapshot.children) {
                    for (place in node.children) {
                        if (place.child("placeName").getValue(String::class.java) == originalPlaceName) {
                            val updateTask = place.ref.updateChildren(updatedPlace)
                            updateTasks.add(updateTask)
                            break
                        }
                    }
                }

                semuaRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (place in snapshot.children) {
                            if (place.child("placeName").getValue(String::class.java) == originalPlaceName) {
                                val updateTask = place.ref.updateChildren(updatedPlace)
                                updateTasks.add(updateTask)
                                break
                            }
                        }

                        Tasks.whenAllComplete(updateTasks).addOnCompleteListener { task ->
                            binding.progressBar.visibility = View.GONE
                            if (task.isSuccessful) {
                                Toast.makeText(this@UpdateDataActivity, "Data berhasil diperbarui di kedua child", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@UpdateDataActivity, "Gagal memperbarui data", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@UpdateDataActivity, "Gagal mendapatkan data dari 'semua'", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@UpdateDataActivity, "Gagal mendapatkan data dari 'tempat'", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        const val PLACE = "place"
        const val ADDRESS = "address"
        const val DESC = "desc"
        const val PHOTO = "photo"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val OPERASIONAL = "operasional"
        const val FASILITAS = "fasilitas"
    }
}


