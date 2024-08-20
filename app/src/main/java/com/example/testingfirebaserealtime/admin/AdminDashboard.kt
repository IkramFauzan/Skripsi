package com.example.testingfirebaserealtime.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.testingfirebaserealtime.MainActivity
import com.example.testingfirebaserealtime.R
import com.example.testingfirebaserealtime.Report
import com.example.testingfirebaserealtime.admin.fragment.ChooseCategoryFragment
import com.example.testingfirebaserealtime.admin.fragment.ListAddDestinationFragment
import com.example.testingfirebaserealtime.admin.fragment.ListReportFragment
import com.example.testingfirebaserealtime.admin.fragment.AddEventFragment
import com.example.testingfirebaserealtime.databinding.ActivityAdminDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AdminDashboard : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            checkUserRole(currentUser.uid)
        }

        binding.bottomNavigationAdmin.setOnItemSelectedListener { menuItem ->

            when(menuItem.itemId) {
                R.id.button_home -> {
                    replaceFragment(ListReportFragment())
                    true
                }
                R.id.button_manage -> {
                    replaceFragment(ListAddDestinationFragment())
                    true
                }
                R.id.button_wisata -> {
                    replaceFragment(ChooseCategoryFragment())
                    true
                }
                R.id.button_article -> {
                    replaceFragment(AddEventFragment())
                    true
                }
                else -> false
            }
        }
        replaceFragment(ListReportFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun checkUserRole(userId: String) {
        db.reference.child("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                val role = snapshot.child("role").getValue(String::class.java)
                if (role != "admin") {
                    // Jika pengguna bukan admin, arahkan ke MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mendapatkan peran pengguna", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }
}
