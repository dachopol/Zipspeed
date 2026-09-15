package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// สเปกของธีม
data class ThemeDefinition(
    val id: String,
    val nameTh: String,
    val nameEn: String,
    val isPremium: Boolean,
    val isDark: Boolean,
    val colors: AppColors
)

// ชุดสีที่ทุกธีมต้องมี
data class AppColors(
    val background: Color,
    val surface: Color,
    val cardBg: Color,
    val primary: Color,
    val accent: Color,
    val textMain: Color,
    val textMuted: Color,
    val border: Color
)

// คลังธีม - รองรับทั้ง Dark Mode และ Light Mode อย่างสมมาตร
object ThemeRegistry {
    val cyberDark = ThemeDefinition(
        id = "cyber_dark",
        nameTh = "โหมดมืดล้ำยุค (Dark Mode)",
        nameEn = "Cyber Dark",
        isPremium = false,
        isDark = true,
        colors = AppColors(
            background = Color(0xFF0B0F19),
            surface = Color(0xFF141A28),
            cardBg = Color(0x1AFFFFFF),
            primary = Color(0xFF00FFD1), // Cyan glow
            accent = Color(0xFFE91E63),  // Magenta glow
            textMain = Color(0xFFFFFFFF),
            textMuted = Color(0xB3FFFFFF),
            border = Color(0x2EFFFFFF)
        )
    )

    val pureLight = ThemeDefinition(
        id = "pure_light",
        nameTh = "โหมดสว่างสดใส (Light Mode)",
        nameEn = "Pure Light",
        isPremium = false,
        isDark = false,
        colors = AppColors(
            background = Color(0xFFF1F5F9),
            surface = Color(0xFFFFFFFF),
            cardBg = Color(0xFFFFFFFF),
            primary = Color(0xFF0284C7), // Vibrant Blue
            accent = Color(0xFFD946EF),  // Vibrant Magenta
            textMain = Color(0xFF0F172A),
            textMuted = Color(0xFF64748B),
            border = Color(0xFFE2E8F0)
        )
    )

    val defaultTheme = cyberDark
    val softEyeComfort = cyberDark
    val futuristicBlue = cyberDark
    val allThemes = listOf(cyberDark, pureLight)

    fun getThemeById(id: String): ThemeDefinition {
        return allThemes.find { it.id == id } ?: defaultTheme
    }
}

