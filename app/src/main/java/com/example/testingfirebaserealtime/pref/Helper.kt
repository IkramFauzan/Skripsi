package com.example.testingfirebaserealtime.pref

import android.content.Context
import android.content.SharedPreferences

class Helper (context: Context) {

    private val PREF_NAME = "dark_mode_on"
    private var sharedPreferences: SharedPreferences
    val editor: SharedPreferences.Editor

    init {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    fun put(key: String, value: Boolean) {
        editor.putBoolean(key, value)
            .apply()
    }

    fun getBoolean(key: String) : Boolean {
        return sharedPreferences.getBoolean(key, false)
    }
}