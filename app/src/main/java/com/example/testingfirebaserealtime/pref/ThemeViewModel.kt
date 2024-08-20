package com.example.testingfirebaserealtime.pref

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeViewModel(context: Context) : ViewModel() {
    private val dataStore = context.dataStore

    val themeSetting: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_KEY] ?: false
    }

    fun saveThemeSetting(isDarkModeActive: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.THEME_KEY] = isDarkModeActive
            }
        }
    }
}

object PreferencesKeys {
    val THEME_KEY = booleanPreferencesKey("theme_key")
}