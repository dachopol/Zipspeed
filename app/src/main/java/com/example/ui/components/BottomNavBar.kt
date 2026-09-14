package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
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
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberInk
import com.example.ui.theme.CyberMuted
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.LocalAppTheme

@Composable
fun BottomNavBar(
    activeTab: NavTab,
    language: Language,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTheme = LocalAppTheme.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        currentTheme.colors.surface.copy(alpha = 0.95f),
                        currentTheme.colors.background
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(currentTheme.colors.primary.copy(alpha = 0.35f), Color.Transparent)
                ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                label = if (language == Language.TH) "วัดสปีด" else "SPEED",
                icon = Icons.Default.Speed,
                isSelected = activeTab == NavTab.HOME,
                testTag = "nav_home",
                onClick = { onTabSelected(NavTab.HOME) },
                modifier = Modifier.weight(1f)
            )

            NavItem(
                label = if (language == Language.TH) "ประวัติ" else "HISTORY",
                icon = Icons.Default.History,
                isSelected = activeTab == NavTab.HISTORY,
                testTag = "nav_history",
                onClick = { onTabSelected(NavTab.HISTORY) },
                modifier = Modifier.weight(1f)
            )

            NavItem(
                label = if (language == Language.TH) "ตั้งค่า" else "SETTINGS",
                icon = Icons.Default.Settings,
                isSelected = activeTab == NavTab.SETTINGS,
                testTag = "nav_settings",
                onClick = { onTabSelected(NavTab.SETTINGS) },
                modifier = Modifier.weight(1f)
            )
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
        targetValue = if (isSelected) activeColor.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "navBg"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else CyberMuted,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "navContent"
    )

    val borderColor = if (isSelected) activeColor.copy(alpha = 0.40f) else Color.Transparent

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(animatedBgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = activeColor),
                onClick = onClick
            )
            .padding(vertical = 8.dp),
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
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                color = animatedContentColor,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

