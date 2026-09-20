package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.NavTab
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.ZipMint

private data class SideNavEntry(
    val tab: NavTab,
    val icon: ImageVector,
    val th: String,
    val en: String
)

private val primarySideEntries = listOf(
    SideNavEntry(NavTab.SPEED, Icons.Default.Speed, "ทดสอบ", "Speed"),
    SideNavEntry(NavTab.VIDEO, Icons.Default.Tv, "วิดีโอ", "Video"),
    SideNavEntry(NavTab.STATUS, Icons.Default.SignalCellularAlt, "สถานะ", "Status"),
    SideNavEntry(NavTab.MAP, Icons.Default.Public, "แผนที่", "Map"),
    SideNavEntry(NavTab.HISTORY, Icons.Default.History, "ประวัติ", "History")
)

@Composable
fun AdaptiveSideNavigation(
    activeTab: NavTab,
    language: Language,
    expanded: Boolean,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current

    if (!expanded) {
        NavigationRail(
            modifier = modifier.fillMaxHeight(),
            containerColor = theme.colors.cardBg
        ) {
            Spacer(Modifier.size(8.dp))
            primarySideEntries.forEach { item ->
                val label = if (language == Language.TH) item.th else item.en
                NavigationRailItem(
                    selected = activeTab == item.tab,
                    onClick = { onTabSelected(item.tab) },
                    icon = { Icon(item.icon, contentDescription = label) },
                    label = { Text(label, fontSize = 10.sp) }
                )
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(220.dp)
            .background(theme.colors.cardBg)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Zipspeed",
            color = theme.colors.textMain,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
        )
        Text(
            text = "by AnakinYoo",
            color = theme.colors.textMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = theme.colors.border
        )
        primarySideEntries.forEach { item ->
            val selected = activeTab == item.tab
            val label = if (language == Language.TH) item.th else item.en
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (selected) ZipMint.copy(alpha = 0.12f)
                        else theme.colors.cardBg
                    )
                    .clickable { onTabSelected(item.tab) }
                    .padding(horizontal = 18.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = label,
                    tint = if (selected) ZipMint else theme.colors.textMuted,
                    modifier = Modifier.size(21.dp)
                )
                Text(
                    text = label,
                    color = if (selected) ZipMint else theme.colors.textMain,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}
