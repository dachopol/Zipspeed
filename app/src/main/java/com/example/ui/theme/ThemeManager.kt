package com.example.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager(context: Context) {
    private val prefs = context.getSharedPreferences("zipspeed_theme_prefs", Context.MODE_PRIVATE)
    
    private val _currentTheme = MutableStateFlow(
        ThemeRegistry.getThemeById(prefs.getString("selected_theme_id", "futuristic_blue") ?: "futuristic_blue")
    )
    val currentTheme: StateFlow<ThemeDefinition> = _currentTheme.asStateFlow()

    fun setTheme(themeId: String) {
        val newTheme = ThemeRegistry.getThemeById(themeId)
        prefs.edit().putString("selected_theme_id", themeId).apply()
        _currentTheme.value = newTheme
    }

    fun toggleNextTheme() {
        val currentIndex = ThemeRegistry.allThemes.indexOfFirst { it.id == _currentTheme.value.id }
        val nextIndex = if (currentIndex >= 0) (currentIndex + 1) % ThemeRegistry.allThemes.size else 0
        setTheme(ThemeRegistry.allThemes[nextIndex].id)
    }
}
