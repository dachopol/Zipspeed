package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppTheme

/**
 * Compact sponsor label. The actionable ad/VIP surface is rendered by the
 * speed gauge, so the dashboard does not present a second competing button.
 */
@Composable
fun AdMobBannerView(
    isVipAdFree: Boolean,
    onRemoveAdsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVipAdFree) return

    val theme = LocalAppTheme.current
    val isDark = theme.isDark

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) Color(0xFF131824) else Color.White)
            .border(
                1.dp,
                if (isDark) Color(0x33FFFFFF) else Color(0xFFE2E8F0),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("admob_banner_view")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFEAB308))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Ad",
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AIS 5G Fibre • เน็ตบ้านไฟเบอร์แท้ 1 Gbps",
                    color = theme.colors.textMain,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "ติดตั้งฟรี รับส่วนลดพิเศษสำหรับผู้ทดสอบความเร็ว",
                    color = theme.colors.textMuted,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}
