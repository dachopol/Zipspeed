package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Local provider for accessing theme anywhere in the UI
val LocalAppTheme = staticCompositionLocalOf { ThemeRegistry.cyberDark }

@Composable
fun ZipspeedTheme(
    theme: ThemeDefinition = ThemeRegistry.cyberDark,
    content: @Composable () -> Unit
) {
    val colors = theme.colors
    val colorScheme = if (theme.isDark) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = Color.Black,
            secondary = colors.accent,
            onSecondary = Color.White,
            background = colors.background,
            onBackground = colors.textMain,
            surface = colors.surface,
            onSurface = colors.textMain,
            surfaceVariant = colors.cardBg,
            onSurfaceVariant = colors.textMuted,
            outline = colors.border
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = Color.White,
            secondary = colors.accent,
            onSecondary = Color.White,
            background = colors.background,
            onBackground = colors.textMain,
            surface = colors.surface,
            onSurface = colors.textMain,
            surfaceVariant = colors.cardBg,
            onSurfaceVariant = colors.textMuted,
            outline = colors.border
        )
    }
    
    CompositionLocalProvider(LocalAppTheme provides theme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
