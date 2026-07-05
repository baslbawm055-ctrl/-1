package com.example

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString("themeMode", "System") ?: "System")
    val themeMode: StateFlow<String> = _themeMode

    private val _language = MutableStateFlow(prefs.getString("language", "English") ?: "English")
    val language: StateFlow<String> = _language

    private val _fontSize = MutableStateFlow(prefs.getString("fontSize", "Medium") ?: "Medium")
    val fontSize: StateFlow<String> = _fontSize

    fun setThemeMode(mode: String) {
        prefs.edit { putString("themeMode", mode) }
        _themeMode.value = mode
    }

    fun setLanguage(lang: String) {
        prefs.edit { putString("language", lang) }
        _language.value = lang
    }

    fun setFontSize(size: String) {
        prefs.edit { putString("fontSize", size) }
        _fontSize.value = size
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsManager(context).also { INSTANCE = it }
            }
        }
    }
}
