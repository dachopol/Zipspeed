package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.ui.components.verticalScrollbar
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.WinePink

@Composable
fun AdFreeScreen(
    isVipAdFree: Boolean,
    language: Language,
    onActivateVip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val isTh = language == Language.TH
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .verticalScrollbar(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("ad_free_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero VIP Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1E1724),
                                Color(0xFF15101A)
                            )
                        )
                    )
                    .border(1.5.dp, ChampagneGold.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(ChampagneGold.copy(alpha = 0.2f))
                            .border(1.dp, ChampagneGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = ChampagneGold,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Zipspeed VIP Ad-Free",
                        color = ChampagneGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isVipAdFree) {
                            if (isTh) "สถานะ: สมาชิก VIP ตลอดชีพ (เปิดใช้งานแล้ว)" else "Status: Lifetime VIP Active"
                        } else {
                            if (isTh) "ปลดล็อกความเร็วสูงสุด ปราศจากโฆษณาตลอดชีพ" else "Unlock max speed & ad-free lifetime experience"
                        },
                        color = if (isVipAdFree) StatusGreen else Color(0xFFD0C4DF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isVipAdFree) Color(0x304CAF50) else WinePink)
                            .border(
                                1.dp,
                                if (isVipAdFree) StatusGreen else ChampagneGold.copy(alpha = 0.5f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = Color.White),
                                onClick = onActivateVip
                            )
                            .testTag("vip_activate_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isVipAdFree) Icons.Default.CheckCircle else Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isVipAdFree) {
                                    if (isTh) "เปิดใช้งาน VIP เรียบร้อย (กดเพื่อสลับ)" else "VIP Active (Tap to toggle)"
                                } else {
                                    if (isTh) "เปิดใช้งาน VIP ทันที (ฟรี)" else "Activate VIP Free Pass"
                                },
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // VIP Benefits Breakdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.colors.cardBg)
                    .border(1.dp, theme.colors.border, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = if (isTh) "สิทธิพิเศษสำหรับสมาชิก Zipspeed VIP" else "VIP Member Privileges",
                        color = theme.colors.textMain,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    val perks = listOf(
                        Triple(
                            Icons.Default.Security,
                            if (isTh) "ไร้โฆษณา 100% (Ad-Free)" else "100% Ad-Free Experience",
                            if (isTh) "ไม่มีแบนเนอร์หรือโฆษณาวิดีโอคั่นระหว่างการทดสอบ" else "Zero banner or interstitial ads during tests"
                        ),
                        Triple(
                            Icons.Default.Speed,
                            if (isTh) "โหมดความแม่นยำสูง (Precision Mode)" else "Unlimited Precision Mode",
                            if (isTh) "ทดสอบ Throughput สูงสุดด้วยขนาดข้อมูลและเธรดเต็มพิกัด" else "Multi-stream throughput test with full payload"
                        ),
                        Triple(
                            Icons.Default.Diamond,
                            if (isTh) "ลำดับความสำคัญเซิร์ฟเวอร์ (Priority CDN)" else "Priority Edge Server Routing",
                            if (isTh) "เชื่อมต่อไปยังโหนด CDN ใดก็ได้โดยไม่ต้องรอคิว" else "Fastest routing to Cloudflare & AWS edge nodes"
                        ),
                        Triple(
                            Icons.Default.Star,
                            if (isTh) "ส่งออกรายงานผลระดับพรีเมียม (HD Export)" else "HD Vector & PDF Export",
                            if (isTh) "บันทึกและแชร์ผลทดสอบพร้อมตราประทับ Zipspeed VIP" else "Share custom branded speed test report cards"
                        )
                    )

                    perks.forEach { (icon, title, desc) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ChampagneGold.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = ChampagneGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    color = theme.colors.textMain,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = desc,
                                    color = theme.colors.textMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}
