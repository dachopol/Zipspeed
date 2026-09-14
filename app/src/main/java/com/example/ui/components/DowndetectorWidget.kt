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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRose
import com.example.util.startActivitySafely

data class DowndetectorService(
    val name: String,
    val categoryTh: String,
    val categoryEn: String,
    val isOk: Boolean = true,
    val reportCount: Int = 0,
    val brandColor: Color = Color(0xFF2F7BFF),
    val url: String
)

@Composable
fun DowndetectorWidget(
    language: Language,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }

    val services = remember {
        listOf(
            DowndetectorService(
                name = "AIS",
                categoryTh = "อินเทอร์เน็ต & เครือข่ายมือถือ",
                categoryEn = "ISP & Mobile Network",
                isOk = true,
                reportCount = 2,
                brandColor = Color(0xFF86B817),
                url = "https://downdetector.in.th/sathana/ais/"
            ),
            DowndetectorService(
                name = "True / DTAC",
                categoryTh = "เครือข่ายมือถือ & ไฟเบอร์",
                categoryEn = "Mobile & Fiber Network",
                isOk = true,
                reportCount = 5,
                brandColor = Color(0xFFE52329),
                url = "https://downdetector.in.th/sathana/true/"
            ),
            DowndetectorService(
                name = "YouTube",
                categoryTh = "วิดีโอสตรีมมิ่ง",
                categoryEn = "Video Streaming",
                isOk = true,
                reportCount = 4,
                brandColor = Color(0xFFFF0000),
                url = "https://downdetector.com/status/youtube/"
            ),
            DowndetectorService(
                name = "Facebook",
                categoryTh = "โซเชียลมีเดีย",
                categoryEn = "Social Media",
                isOk = true,
                reportCount = 12,
                brandColor = Color(0xFF1877F2),
                url = "https://downdetector.com/status/facebook/"
            ),
            DowndetectorService(
                name = "LINE",
                categoryTh = "แอปแชท & สื่อสาร",
                categoryEn = "Messaging & Calls",
                isOk = true,
                reportCount = 1,
                brandColor = Color(0xFF06C755),
                url = "https://downdetector.in.th/sathana/line/"
            ),
            DowndetectorService(
                name = "Netflix",
                categoryTh = "ภาพยนตร์ & สตรีมมิ่ง",
                categoryEn = "Streaming Entertainment",
                isOk = true,
                reportCount = 3,
                brandColor = Color(0xFFE50914),
                url = "https://downdetector.com/status/netflix/"
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CyberPanel)
            .border(1.dp, Color(0x1F2F7BFF), RoundedCornerShape(20.dp))
            .padding(14.dp)
            .testTag("downdetector_widget")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB74D))
                    )
                    Text(
                        text = "HAVING INTERNET PROBLEMS?",
                        color = Color(0xFFFFB74D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                }
                Text(
                    text = if (language == Language.TH) "เช็คบริการที่มีปัญหาจาก Downdetector" else "Check service outages from Downdetector",
                    color = CyberInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Downdetector Mini Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x22FFB74D))
                    .border(1.dp, Color(0x44FFB74D), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "DOWNDETECTOR",
                    color = Color(0xFFFFB74D),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Services list
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            services.forEach { service ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x15FFFFFF))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = NeonBlue)
                        ) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(service.url))
                            context.startActivitySafely(intent)
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("downdetector_service_${service.name.lowercase().replace(" ", "_")}"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Brand initial circle
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(service.brandColor.copy(alpha = 0.25f))
                                .border(1.dp, service.brandColor.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = service.name.take(1),
                                color = service.brandColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column {
                            Text(
                                text = service.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (language == Language.TH) service.categoryTh else service.categoryEn,
                                color = CyberMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Status Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (service.isOk) NeonGreen else NeonRose)
                        )
                        Text(
                            text = if (service.isOk) {
                                if (language == Language.TH) "ปกติ" else "Operational"
                            } else {
                                if (language == Language.TH) "มีปัญหา" else "Outage"
                            },
                            color = if (service.isOk) NeonGreen else NeonRose,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open Downdetector",
                            tint = CyberMuted,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Open Downdetector Outages Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x182F7BFF))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = NeonBlue)
                ) {
                    val url = if (language == Language.TH) "https://downdetector.in.th/" else "https://downdetector.com/"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivitySafely(intent)
                }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language == Language.TH) "ดูสถานะรายงานทั้งหมดบน Downdetector →" else "Check all service outages on Downdetector →",
                color = Color(0xFFA9C6FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
