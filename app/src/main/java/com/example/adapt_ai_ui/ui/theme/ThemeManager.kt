package com.example.adapt_ai_ui.ui.theme

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.core.content.edit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode(val title: String) {
    SYSTEM("Follow System"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

class ThemeManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("adapt_ai_theme_prefs", Context.MODE_PRIVATE)

    var currentThemeMode by mutableStateOf(loadThemeMode())
        private set

    private fun loadThemeMode(): ThemeMode {
        val savedName = prefs.getString("theme_mode", ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(savedName ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        currentThemeMode = mode
        prefs.edit { putString("theme_mode", mode.name) }
    }
}

val LocalThemeManager = compositionLocalOf<ThemeManager?> { null }
