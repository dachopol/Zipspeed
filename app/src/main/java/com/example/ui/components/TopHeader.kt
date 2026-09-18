package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.StatusGreen

/**
 * Decluttered, luxurious top header:
 * - "Zipspeed" title with smaller "by AnakinYoo" label
 * - Subtle live status pulse
 * - Minimalist Ad-Free VIP pill/icon button
 * - Version, Language, and Theme toggles moved to Settings screen for clean hierarchy
 */
@Composable
fun TopHeader(
    language: Language,
    reducedMotion: Boolean,
    isDarkTheme: Boolean = true,
    isVipAdFree: Boolean = false,
    isGpsActive: Boolean = true,
    onLanguageChange: (Language) -> Unit = {},
    onToggleDarkLight: () -> Unit = {},
    onOpenVipModal: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onToggleGps: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentTheme = LocalAppTheme.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by if (reducedMotion) {
        remember { androidx.compose.runtime.mutableFloatStateOf(1.0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dotPulse"
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & "by AnakinYoo"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Live Status Indicator Dot (Subtle Green)
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(StatusGreen)
            )

            Column {
                Text(
                    text = "Zipspeed",
                    color = currentTheme.colors.textMain,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold, // 600
                    letterSpacing = (-0.3).sp,
                    modifier = Modifier.testTag("app_title")
                )
                Text(
                    text = "by AnakinYoo",
                    color = currentTheme.colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal, // 400
                    letterSpacing = 0.2.sp
                )
            }
        }

        // Header actions: Settings + VIP. These replace secondary bottom-nav destinations.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(currentTheme.colors.surface.copy(alpha = 0.78f))
                    .border(1.dp, currentTheme.colors.border, RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = currentTheme.colors.primary),
                        onClick = onOpenSettings
                    )
                    .testTag("settings_header_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = if (language == Language.TH) "การตั้งค่า" else "Settings",
                    tint = currentTheme.colors.textMuted,
                    modifier = Modifier.size(19.dp)
                )
            }

            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isVipAdFree) ChampagneGold.copy(alpha = 0.12f)
                        else currentTheme.colors.surface.copy(alpha = 0.78f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isVipAdFree) ChampagneGold.copy(alpha = 0.45f)
                        else currentTheme.colors.border,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = ChampagneGold),
                        onClick = onOpenVipModal
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("vip_header_btn"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "VIP Ad-Free",
                        tint = if (isVipAdFree) StatusGreen else ChampagneGold,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (isVipAdFree) "VIP" else "Ad-Free",
                        color = if (isVipAdFree) StatusGreen else ChampagneGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
