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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.NavTab
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.ZipMint

/**
 * Five primary destinations stay visible without horizontal scrolling.
 * Settings is available in the top header and Ad-Free/VIP is available there too.
 */
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(currentTheme.colors.cardBg.copy(alpha = 0.98f))
            .border(
                width = 1.dp,
                color = currentTheme.colors.border,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 7.dp)
            .testTag("bottom_nav_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                label = if (language == Language.TH) "ทดสอบ" else "Speed",
                icon = Icons.Default.Speed,
                isSelected = activeTab == NavTab.SPEED,
                testTag = "nav_speed",
                onClick = { onTabSelected(NavTab.SPEED) },
                modifier = Modifier.weight(1f)
            )
            NavItem(
                label = if (language == Language.TH) "วิดีโอ" else "Video",
                icon = Icons.Default.Tv,
                isSelected = activeTab == NavTab.VIDEO,
                testTag = "nav_video",
                onClick = { onTabSelected(NavTab.VIDEO) },
                modifier = Modifier.weight(1f)
            )
            NavItem(
                label = if (language == Language.TH) "สถานะ" else "Status",
                icon = Icons.Default.SignalCellularAlt,
                isSelected = activeTab == NavTab.STATUS,
                testTag = "nav_status",
                onClick = { onTabSelected(NavTab.STATUS) },
                modifier = Modifier.weight(1f)
            )
            NavItem(
                label = if (language == Language.TH) "แผนที่" else "Map",
                icon = Icons.Default.Public,
                isSelected = activeTab == NavTab.MAP,
                testTag = "nav_map",
                onClick = { onTabSelected(NavTab.MAP) },
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
    val activeColor = ZipMint
    val animatedBgColor by animateColorAsState(
        targetValue = if (isSelected) activeColor.copy(alpha = 0.13f) else Color.Transparent,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "navBg"
    )
    val animatedContentColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else currentTheme.colors.textMuted,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "navContent"
    )

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(animatedBgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = activeColor),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.testTag(testTag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = animatedContentColor,
                modifier = Modifier.size(21.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                color = animatedContentColor,
                fontSize = 10.sp,
                maxLines = 1,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}
