package com.example.testingfirebaserealtime.admin.fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.testingfirebaserealtime.Article
import com.example.testingfirebaserealtime.EventAd
import com.example.testingfirebaserealtime.databinding.FragmentAddEventBinding
import com.example.testingfirebaserealtime.detail.DetailKulinerActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class AddEventFragment : Fragment() {

    private lateinit var binding: FragmentAddEventBinding
    private lateinit var db: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var database: DatabaseReference
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddEventBinding.inflate(inflater, container, false)
        db = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        database = db.reference.child("eventAds")

        binding.imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        binding.eventDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.uploadButton.setOnClickListener {
            val name = binding.eventTitle.text.toString()
            val address = binding.eventAddress.text.toString()
            val description = binding.eventDesc.text.toString()
            val schedule = binding.eventDate.text.toString()
            if (name.isNotEmpty() && address.isNotEmpty() && description.isNotEmpty() && schedule.isNotEmpty() && imageUri != null) {
                uploadImageToFirebase(name, address, description, schedule)
            } else {
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    private fun uploadImageToFirebase(name: String, address: String, description: String, schedule: String) {
        val storageRef = storage.reference.child("event_images/${UUID.randomUUID()}")
        imageUri?.let {
            storageRef.putFile(it)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveEventToDatabase(uri.toString(), name, address, schedule, description)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveEventToDatabase(imageUrl: String, eventName: String, eventAddress: String, eventSchedule: String, eventDescription: String) {
        val eventRef = database.push()
        val event = EventAd(imageUrl, eventName, eventSchedule, eventDescription, eventAddress)

        eventRef.setValue(event)
            .addOnSuccessListener {
                Toast.makeText(context, "Event uploaded successfully", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to upload event", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        binding.eventTitle.text.clear()
        binding.eventAddress.text.clear()
        binding.eventDesc.text.clear()
        binding.eventDate.text = ""
        binding.imageView.setImageURI(null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            binding.imageView.setImageURI(imageUri)
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                    GregorianCalendar(selectedYear, selectedMonth, selectedDay).time
                )
                binding.eventDate.text = selectedDate
            },
            year, month, day
        )
        datePickerDialog.show()
    }
}



