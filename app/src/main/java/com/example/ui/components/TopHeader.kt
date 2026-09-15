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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WorkspacePremium
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
import com.example.ui.theme.LocalAppTheme

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
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dotPulse"
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & Pulsing Status Dot
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Color(0xFF34C759))
            )
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Zipspeed",
                        color = currentTheme.colors.textMain,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.testTag("app_title")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00FFD1).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF00FFD1).copy(alpha = 0.40f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "v${com.example.BuildConfig.VERSION_NAME}",
                            color = Color(0xFF00FFD1),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "NETWORK • LIVE",
                    color = currentTheme.colors.textMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }

        // Action Pill Bar: [GPS Mode] [Dark/Light] [VIP Ad-Free] [TH/EN]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Dark / Light Mode Toggle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDarkTheme) Color(0x22FFFFFF) else Color(0xFFF1F5F9)
                    )
                    .border(
                        1.dp,
                        if (isDarkTheme) Color(0x33FFFFFF) else Color(0xFFCBD5E1),
                        CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color(0xFF00FFD1)),
                        onClick = onToggleDarkLight
                    )
                    .testTag("theme_toggle_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Dark/Light Mode",
                    tint = if (isDarkTheme) Color(0xFFFFD54F) else Color(0xFF334155),
                    modifier = Modifier.size(17.dp)
                )
            }

            // VIP Ad-Free Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isVipAdFree) Color(0xFF34C759).copy(alpha = 0.20f) else Color(0xFFFF9500).copy(alpha = 0.15f)
                    )
                    .border(
                        1.dp,
                        if (isVipAdFree) Color(0xFF34C759).copy(alpha = 0.50f) else Color(0xFFFF9500).copy(alpha = 0.40f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color(0xFFFF9500)),
                        onClick = onOpenVipModal
                    )
                    .padding(horizontal = 7.dp, vertical = 5.dp)
                    .testTag("vip_ad_free_btn"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "VIP Ad Free",
                        tint = if (isVipAdFree) Color(0xFF34C759) else Color(0xFFFF9500),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (isVipAdFree) "VIP" else (if (language == Language.TH) "ไม่มีโฆษณา" else "Ad-Free"),
                        color = if (isVipAdFree) Color(0xFF34C759) else Color(0xFFFF9500),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Language Switcher (ไทย | EN)
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isDarkTheme) Color(0x18FFFFFF) else Color(0xFFF1F5F9))
                    .border(1.dp, if (isDarkTheme) Color(0x22FFFFFF) else Color(0xFFCBD5E1), CircleShape)
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Language.entries.forEach { lang ->
                    val isActive = lang == language
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isActive) Color(0xFF00FFD1) else Color.Transparent)
                            .clickable { onLanguageChange(lang) }
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lang.displayName,
                            color = if (isActive) Color(0xFF0B0F19) else currentTheme.colors.textMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
