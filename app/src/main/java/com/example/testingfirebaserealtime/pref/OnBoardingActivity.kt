package com.example.testingfirebaserealtime.pref

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.testingfirebaserealtime.auth.SignActivity
import com.example.testingfirebaserealtime.auth.SignUpActivity
import com.example.testingfirebaserealtime.databinding.ActivityOnBoardingBinding

class OnBoardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnBoardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.Login.setOnClickListener {
            // Start the login activity
            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)

            // Set the flag that the app has been opened before
            val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean("isFirstRun", false)
                apply()
            }
        }

        binding.Register.setOnClickListener {
            // Start the register activity
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)

            // Set the flag that the app has been opened before
            val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean("isFirstRun", false)
                apply()
            }
        }
    }
}