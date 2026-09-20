package com.example.ui.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.ui.theme.CyberInk
import com.example.ui.theme.CyberMuted
import com.example.ui.theme.CyberPanel
import com.example.ui.theme.NeonBlue
import com.example.util.startActivitySafely

private data class OutageLink(
    val name: String,
    val categoryTh: String,
    val categoryEn: String,
    val brandColor: Color,
    val url: String
)

@Composable
fun DowndetectorWidget(
    language: Language,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTh = language == Language.TH

    // Directory only: Zipspeed does not scrape or invent live outage counts/status.
    val services = remember {
        listOf(
            OutageLink("AIS", "อินเทอร์เน็ต & มือถือ", "ISP & Mobile", Color(0xFF86B817), "https://downdetector.in.th/sathana/ais/"),
            OutageLink("True", "อินเทอร์เน็ต & มือถือ", "ISP & Mobile", Color(0xFFE52329), "https://downdetector.in.th/sathana/true/"),
            OutageLink("YouTube", "วิดีโอสตรีมมิ่ง", "Video Streaming", Color(0xFFFF0000), "https://downdetector.com/status/youtube/"),
            OutageLink("Facebook", "โซเชียลมีเดีย", "Social Media", Color(0xFF1877F2), "https://downdetector.com/status/facebook/"),
            OutageLink("LINE", "แชท & สื่อสาร", "Messaging", Color(0xFF06C755), "https://downdetector.in.th/sathana/line/"),
            OutageLink("Netflix", "สตรีมมิ่ง", "Streaming", Color(0xFFE50914), "https://downdetector.com/status/netflix/")
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CyberPanel, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0x1F2F7BFF), RoundedCornerShape(20.dp))
            .padding(14.dp)
            .testTag("downdetector_widget"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (isTh) "ลิงก์ตรวจสถานะบริการภายนอก" else "External outage-status links",
            color = CyberInk,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isTh)
                "Zipspeed ไม่สร้างจำนวนรายงานหรือสถานะล่มเอง แตะรายการเพื่อเปิดแหล่งข้อมูลภายนอก"
            else
                "Zipspeed does not invent outage counts or service status. Tap an item to open the external source.",
            color = CyberMuted,
            fontSize = 10.sp
        )

        services.forEach { service ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x15FFFFFF), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = NeonBlue)
                    ) {
                        context.startActivitySafely(Intent(Intent.ACTION_VIEW, Uri.parse(service.url)))
                    }
                    .padding(horizontal = 10.dp, vertical = 9.dp)
                    .testTag("outage_link_${service.name.lowercase()}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(service.brandColor.copy(alpha = 0.22f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = service.name.take(1),
                            color = service.brandColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(service.name, color = CyberInk, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (isTh) service.categoryTh else service.categoryEn,
                            color = CyberMuted,
                            fontSize = 10.sp
                        )
                    }
                }
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = if (isTh) "เปิดเว็บไซต์ภายนอก" else "Open external website",
                    tint = CyberMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.size(2.dp))
        Text(
            text = if (isTh) "สถานะจริงขึ้นกับข้อมูลบนเว็บไซต์ปลายทาง" else "Live status is provided by the destination website.",
            color = CyberMuted,
            fontSize = 9.sp
        )
    }
}
