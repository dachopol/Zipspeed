package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// สเปกของธีม
data class ThemeDefinition(
    val id: String,
    val nameTh: String,
    val nameEn: String,
    val isPremium: Boolean,
    val colors: AppColors
)

// ชุดสีที่ทุกธีมต้องมี
data class AppColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val accent: Color,
    val textMain: Color,
    val textMuted: Color
)

// คลังธีม - ใช้ธีมละมุน สบายตา เป็นค่าหลัก
object ThemeRegistry {
    val softEyeComfort = ThemeDefinition(
        id = "soft_eye_comfort",
        nameTh = "ละมุน สบายตา (Eye-Care Soft)",
        nameEn = "Soft & Eye-Care",
        isPremium = false,
        colors = AppColors(
            background = CyberBg,
            surface = CyberSurface,
            primary = NeonBlue,
            accent = NeonGreen,
            textMain = CyberInk,
            textMuted = CyberMuted
        )
    )

    val defaultSoftTheme = softEyeComfort
    val futuristicBlue = softEyeComfort
    val allThemes = listOf(softEyeComfort)
    fun getThemeById(id: String) = softEyeComfort
}
