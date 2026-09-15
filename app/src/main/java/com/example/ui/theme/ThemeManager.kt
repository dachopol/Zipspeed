package com.example.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager(context: Context) {
    private val prefs = context.getSharedPreferences("zipspeed_theme_prefs", Context.MODE_PRIVATE)
    
    private val _currentTheme = MutableStateFlow(
        ThemeRegistry.getThemeById(prefs.getString("selected_theme_id", "cyber_dark") ?: "cyber_dark")
    )
    val currentTheme: StateFlow<ThemeDefinition> = _currentTheme.asStateFlow()

    fun setTheme(themeId: String) {
        val newTheme = ThemeRegistry.getThemeById(themeId)
        prefs.edit().putString("selected_theme_id", themeId).apply()
        _currentTheme.value = newTheme
    }

    fun toggleDarkLight() {
        val nextThemeId = if (_currentTheme.value.isDark) ThemeRegistry.pureLight.id else ThemeRegistry.cyberDark.id
        setTheme(nextThemeId)
    }

    fun toggleNextTheme() {
        toggleDarkLight()
    }
}
