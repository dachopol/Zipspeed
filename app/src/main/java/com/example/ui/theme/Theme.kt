package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val ZipspeedDarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0x2E3B82F6),
    onPrimaryContainer = Color(0xFFDCE7FF),
    secondary = NeonGreen,
    onSecondary = Color(0xFF071510),
    secondaryContainer = Color(0x2610B981),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = NeonPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0x268B5CF6),
    onTertiaryContainer = Color(0xFFEDE9FE),
    background = CyberBg,
    onBackground = CyberInk,
    surface = CyberSurface,
    onSurface = CyberInk,
    surfaceVariant = CyberPanel,
    onSurfaceVariant = CyberMuted,
    outline = CyberLine,
    outlineVariant = CyberLineActive
)

// Local provider for accessing theme anywhere in the UI
val LocalAppTheme = staticCompositionLocalOf { ThemeRegistry.futuristicBlue }

@Composable
fun ZipspeedTheme(
    theme: ThemeDefinition = ThemeRegistry.futuristicBlue,
    content: @Composable () -> Unit
) {
    val colors = theme.colors
    val colorScheme = darkColorScheme(
        primary = colors.primary,
        onPrimary = Color.White,
        secondary = colors.accent,
        onSecondary = Color.Black,
        background = colors.background,
        onBackground = colors.textMain,
        surface = colors.surface,
        onSurface = colors.textMain,
        surfaceVariant = colors.surface,
        onSurfaceVariant = colors.textMuted,
        outline = colors.textMuted
    )
    
    CompositionLocalProvider(LocalAppTheme provides theme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
