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

// คลังธีม - ออกแบบตามทิศทางพรีเมียมหรูหรา (Dark Deep Navy #0B1120 & Light Ivory #F5F3EF)
object ThemeRegistry {
    val cyberDark = ThemeDefinition(
        id = "cyber_dark",
        nameTh = "โหมดมืดพรีเมียม (Dark Mode)",
        nameEn = "Deep Navy Dark",
        isPremium = false,
        isDark = true,
        colors = AppColors(
            background = DeepNavy,       // #0B1120
            surface = DarkCard,          // #141E30
            cardBg = DarkCard,           // #141E30
            primary = ChampagneGold,     // #D6BC8A
            accent = WinePink,           // #A83D65
            textMain = TextWhite,        // #F2F4F8
            textMuted = TextMuted,       // #A7B3C6
            border = Color(0x1FFFFFFF)   // เส้นขอบบางละเอียด
        )
    )

    val pureLight = ThemeDefinition(
        id = "pure_light",
        nameTh = "โหมดสว่างงาช้าง (Light Mode)",
        nameEn = "Ivory Light",
        isPremium = false,
        isDark = false,
        colors = AppColors(
            background = IvoryBg,         // #F5F3EF
            surface = LightCard,          // #FFFFFF
            cardBg = LightCard,           // #FFFFFF
            primary = ChampagneBronze,    // #96783C
            accent = WinePink,            // #A83D65
            textMain = LightTextMain,     // #0B1120
            textMuted = LightTextMuted,   // #64748B
            border = Color(0xFFE2E8F0)    // เส้นขอบบางสว่าง
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
