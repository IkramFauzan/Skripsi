  package com.example.testingfirebaserealtime

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.testingfirebaserealtime.admin.AdminDashboard
import com.example.testingfirebaserealtime.auth.SignActivity
import com.example.testingfirebaserealtime.databinding.ActivityMainBinding
import com.example.testingfirebaserealtime.fragment.HomeFragment
import com.example.testingfirebaserealtime.fragment.ListArticleFragment
import com.example.testingfirebaserealtime.fragment.ProfileFragment
import com.example.testingfirebaserealtime.fragment.SearchFragment
import com.example.testingfirebaserealtime.pref.Helper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

  class MainActivity : AppCompatActivity() {

      private lateinit var binding: ActivityMainBinding
      private val preferences by lazy { Helper(this) }
      private lateinit var auth: FirebaseAuth
      private lateinit var db: FirebaseDatabase

      // Key untuk menyimpan fragment aktif di Bundle
      private val ACTIVE_FRAGMENT_KEY = "active_fragment_key"

      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)

          binding = ActivityMainBinding.inflate(layoutInflater)
          setContentView(binding.root)

          auth = FirebaseAuth.getInstance()

          if (auth.currentUser == null) {
              // Pengguna belum login, arahkan ke halaman login
              val intent = Intent(this, SignActivity::class.java)
              startActivity(intent)
              finish()
              return
          }

          db = FirebaseDatabase.getInstance()

          when (preferences.getBoolean("dark_mode_on")) {
              true -> {
                  preferences.put("dark_mode_on", true)
                  AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
              }
              false -> {
                  preferences.put("dark_mode_on", false)
                  AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
              }
          }

          binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
              when (menuItem.itemId) {
                  R.id.button_home -> {
                      replaceFragment(HomeFragment())
                      true
                  }
                  R.id.button_search -> {
                      replaceFragment(SearchFragment())
                      true
                  }
                  R.id.button_profile -> {
                      replaceFragment(ProfileFragment())
                      true
                  }
                  else -> false
              }
          }

          // Memulihkan fragment aktif jika ada
          if (savedInstanceState == null) {
              replaceFragment(HomeFragment())
          } else {
              val activeFragment = savedInstanceState.getString(ACTIVE_FRAGMENT_KEY)
              when (activeFragment) {
                  HomeFragment::class.java.simpleName -> replaceFragment(HomeFragment())
                  SearchFragment::class.java.simpleName -> replaceFragment(SearchFragment())
                  ProfileFragment::class.java.simpleName -> replaceFragment(ProfileFragment())
              }
          }
      }

      private fun replaceFragment(fragment: Fragment) {
          supportFragmentManager
              .beginTransaction()
              .replace(R.id.fragment_container, fragment)
              .commit()
      }

      override fun onSaveInstanceState(outState: Bundle) {
          super.onSaveInstanceState(outState)
          // Menyimpan nama fragment aktif ke dalam Bundle
          val activeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
          activeFragment?.let {
              outState.putString(ACTIVE_FRAGMENT_KEY, it::class.java.simpleName)
          }
      }
  }
