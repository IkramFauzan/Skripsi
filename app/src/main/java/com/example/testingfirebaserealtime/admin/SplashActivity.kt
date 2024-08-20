package com.example.testingfirebaserealtime.admin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testingfirebaserealtime.MainActivity
import com.example.testingfirebaserealtime.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Anda dapat membuat layout sederhana untuk aktivitas ini

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser
            if (currentUser != null) {
                checkUserRole(currentUser.uid)
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, 2000) // 2000 ms = 2 detik delay
    }

    private fun checkUserRole(userId: String) {
        db.reference.child("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                val role = snapshot.child("role").getValue(String::class.java)
                if (role == "admin") {
                    // Jika pengguna adalah admin, arahkan ke halaman admin
                    startActivity(Intent(this, AdminDashboard::class.java))
                } else {
                    // Jika pengguna bukan admin, arahkan ke halaman utama
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mendapatkan peran pengguna", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }
}
