package com.example.testingfirebaserealtime.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.activity.CuacaActivity
import com.example.testingfirebaserealtime.activity.ListFavoriteActivity
import com.example.testingfirebaserealtime.activity.ProfileUlasanActivity
import com.example.testingfirebaserealtime.admin.ListAddDestination
import com.example.testingfirebaserealtime.auth.SignActivity
import com.example.testingfirebaserealtime.auth.SignUpActivity
import com.example.testingfirebaserealtime.databinding.FragmentProfileBinding
import com.example.testingfirebaserealtime.pref.Helper
import com.example.testingfirebaserealtime.profile.AddDestinationActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var profileImageUrl: String? = null

    private val PICK_IMAGE_REQUEST = 71

    private val preferences by lazy { Helper(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        Log.d("ProfileActivity", "UID: $userId")

        val usersRef = database.getReference("users")

        userId?.let {
            usersRef.child(it).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val context = context ?: return
                    val email = snapshot.child("email").getValue(String::class.java)
                    val username = snapshot.child("username").getValue(String::class.java)

                    binding.emailTextView.text = email
                    binding.usernameTextView.text = username

                    profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(context)
                            .load(profileImageUrl)
                            .into(binding.profileImageView)
                    }

                    username?.let {username ->
                        binding.userUlasan.setOnClickListener {
                            val intent = Intent(requireContext(), ProfileUlasanActivity::class.java)
                            intent.putExtra("username", username)
                            startActivity(intent)
                        }

                        binding.userFavorite.setOnClickListener {
                            val intent = Intent(activity, ListFavoriteActivity::class.java)
                            intent.putExtra("uid", userId)
                            startActivity(intent)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Failed to read user data", error.toException())
                }
            })


        }

        binding.userFavorite.setOnClickListener {
            val intent = Intent(activity, ListFavoriteActivity::class.java)
            intent.putExtra("username", userId)
            startActivity(intent)
        }

        when(preferences.getBoolean("dark_mode_on")) {
            true -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            false -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.themeSwitch.isChecked = preferences.getBoolean("dark_mode_on")

        binding.themeSwitch.setOnCheckedChangeListener { compoundButton, b ->
            when(b) {
                true -> {
                    preferences.put("dark_mode_on", true)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                false -> {
                    preferences.put("dark_mode_on", false)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }

        binding.logout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), SignActivity::class.java)
            startActivity(intent)
        }

        binding.add.setOnClickListener {
            val intent = Intent(requireContext(), AddDestinationActivity::class.java)
            startActivity(intent)
        }

//        binding.admin.setOnClickListener {
//            val intent = Intent(requireContext(), ListAddDestination::class.java)
//            startActivity(intent)
//        }

        binding.profileImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.daftar.visibility = if (currentUser != null) View.GONE else View.VISIBLE
        binding.daftar1.visibility = if (currentUser != null) View.GONE else View.VISIBLE

        binding.daftar.setOnClickListener {
            val intent = Intent(requireContext(), SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.cuaca.setOnClickListener {
            val intent = Intent(requireContext(), CuacaActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val filePath = data.data
            filePath?.let {
                val userId = auth.currentUser?.uid ?: return
                val storageRef = storage.reference.child("profileImages/$userId.jpg")

                val uploadTask = storageRef.putFile(it)
                uploadTask.addOnSuccessListener { taskSnapshot ->
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
                        saveProfileImageUrlToDatabase(downloadUrl, userId)
                    }.addOnFailureListener { e ->
                        Log.e("ProfileFragment", "Failed to get download URL", e)
                    }
                }.addOnFailureListener { e ->
                    Log.e("ProfileFragment", "Failed to upload image", e)
                }
            }
        }
    }

    private fun saveProfileImageUrlToDatabase(downloadUrl: String, userId: String) {
        val usersRef = database.reference.child("users").child(userId)
        usersRef.child("profileImageUrl").setValue(downloadUrl).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Update the profile image in the UI
                Glide.with(this)
                    .load(downloadUrl)
                    .into(binding.profileImageView)
            } else {
                Log.e("ProfileFragment", "Failed to save profile image URL", task.exception)
            }
        }
    }
}

