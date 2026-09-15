package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.NavTab
import com.example.ui.theme.LocalAppTheme

@Composable
fun BottomNavBar(
    activeTab: NavTab,
    language: Language,
    onTabSelected: (NavTab) -> Unit,
    isSecurityShieldActive: Boolean = true,
    onToggleSecurityShield: (Boolean) -> Unit = {},
    onSecurityClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentTheme = LocalAppTheme.current
    val isDark = currentTheme.isDark

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isDark) Color(0xFF0D111A).copy(alpha = 0.95f) else Color(0xFFFFFFFF).copy(alpha = 0.95f)
            )
            .border(
                width = 1.dp,
                color = if (isDark) Color(0x33FFFFFF) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("bottom_nav_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: 3 Primary Navigation Buttons
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                NavItem(
                    label = if (language == Language.TH) "ทดสอบ" else "Test",
                    icon = Icons.Default.Speed,
                    isSelected = activeTab == NavTab.HOME,
                    testTag = "nav_home",
                    onClick = { onTabSelected(NavTab.HOME) },
                    modifier = Modifier.weight(1f)
                )

                NavItem(
                    label = if (language == Language.TH) "ประวัติ" else "History",
                    icon = Icons.Default.History,
                    isSelected = activeTab == NavTab.HISTORY,
                    testTag = "nav_history",
                    onClick = { onTabSelected(NavTab.HISTORY) },
                    modifier = Modifier.weight(1f)
                )

                NavItem(
                    label = if (language == Language.TH) "การตั้งค่า" else "Settings",
                    icon = Icons.Default.Settings,
                    isSelected = activeTab == NavTab.SETTINGS,
                    testTag = "nav_settings",
                    onClick = { onTabSelected(NavTab.SETTINGS) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Footer Security Shield Section (Matches HTML .footer-security)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSecurityShieldActive) {
                            Color(0xFF00FFD1).copy(alpha = 0.12f)
                        } else {
                            if (isDark) Color(0x18FFFFFF) else Color(0xFFF1F5F9)
                        }
                    )
                    .border(
                        1.dp,
                        if (isSecurityShieldActive) Color(0xFF00FFD1).copy(alpha = 0.35f) else Color(0x22FFFFFF),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color(0xFF00FFD1)),
                        onClick = onSecurityClick
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("footer_security_section"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security Shield",
                    tint = if (isSecurityShieldActive) Color(0xFF00FFD1) else Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )

                Column {
                    Text(
                        text = if (language == Language.TH) "ความปลอดภัย" else "Security",
                        color = currentTheme.colors.textMain,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Interactive ON/OFF Switch Pill
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSecurityShieldActive) Color(0xFF00FFD1) else Color(0xFF475569)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onToggleSecurityShield(!isSecurityShieldActive) }
                        )
                        .padding(2.dp)
                        .testTag("security_switch"),
                    contentAlignment = if (isSecurityShieldActive) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTheme = LocalAppTheme.current
    val activeColor = currentTheme.colors.primary

    val animatedBgColor by animateColorAsState(
        targetValue = if (isSelected) activeColor.copy(alpha = 0.16f) else Color.Transparent,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "navBg"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else currentTheme.colors.textMuted,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "navContent"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(animatedBgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = activeColor),
                onClick = onClick
            )
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.testTag(testTag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = animatedContentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = animatedContentColor,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
