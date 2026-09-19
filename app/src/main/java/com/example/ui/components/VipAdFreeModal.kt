package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VipAdFreeModal(
    isVipAdFree: Boolean,
    onDismiss: () -> Unit,
    onPurchaseVip: (planName: String) -> Unit,
    onWatchAdForTempVip: () -> Unit
) {
    val theme = LocalAppTheme.current
    val isDark = theme.isDark
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPlan by remember { mutableStateOf("lifetime") } // "lifetime" or "monthly"
    var paymentMethod by remember { mutableStateOf("promptpay") }
    var showSuccessBanner by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF0F1522) else Color(0xFFFFFFFF),
        scrimColor = Color(0x99000000)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("vip_ad_free_modal")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFE91E63), Color(0xFF00FFD1))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "เติมเงินปิดโฆษณาถาวร",
                            color = theme.colors.textMain,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "VIP AD-FREE • ปลดล็อคความเร็วเต็มพิกัด",
                            color = theme.colors.textMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = theme.colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Anti-Tamper Security Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x1A10B981))
                    .border(1.dp, Color(0x4D10B981), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ระบบป้องกันสิทธิ์แท้ (Anti-Tamper): เข้ารหัส SHA-256 ป้องกัน Lucky Patcher",
                        color = if (isDark) Color(0xFFD1FAE5) else Color(0xFF065F46),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Plan Selector: Lifetime vs Monthly
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Lifetime Plan (Recommended)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (selectedPlan == "lifetime") {
                                if (isDark) Color(0x2E00FFD1) else Color(0x1A0284C7)
                            } else {
                                if (isDark) Color(0x12FFFFFF) else Color(0xFFF8FAFC)
                            }
                        )
                        .border(
                            2.dp,
                            if (selectedPlan == "lifetime") theme.colors.primary else Color(0x22FFFFFF),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedPlan = "lifetime" }
                        .padding(12.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE91E63))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "คุ้มค่าที่สุด",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ถาวรตลอดชีพ",
                            color = theme.colors.textMain,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "฿39 บาท",
                            color = theme.colors.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "จ่ายครั้งเดียว ไม่มีโฆษณาตลอดไป",
                            color = theme.colors.textMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Monthly Plan
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (selectedPlan == "monthly") {
                                if (isDark) Color(0x2E00FFD1) else Color(0x1A0284C7)
                            } else {
                                if (isDark) Color(0x12FFFFFF) else Color(0xFFF8FAFC)
                            }
                        )
                        .border(
                            2.dp,
                            if (selectedPlan == "monthly") theme.colors.primary else Color(0x22FFFFFF),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedPlan = "monthly" }
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "รายเดือน",
                            color = theme.colors.textMain,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "฿19 / ด.",
                            color = theme.colors.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "ยกเลิกเมื่อไหร่ก็ได้ ไม่มีข้อผูกมัด",
                            color = theme.colors.textMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Payment Methods
            Text(
                text = "ช่องทางชำระเงิน:",
                color = theme.colors.textMain,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "promptpay" to "พร้อมเพย์ QR",
                    "truemoney" to "TrueMoney",
                    "play" to "Google Play"
                ).forEach { (id, label) ->
                    val isSel = paymentMethod == id
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSel) theme.colors.primary.copy(alpha = 0.2f)
                                else if (isDark) Color(0x14FFFFFF) else Color(0xFFF1F5F9)
                            )
                            .border(
                                1.dp,
                                if (isSel) theme.colors.primary else Color(0x1FFFFFFF),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { paymentMethod = id }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) theme.colors.primary else theme.colors.textMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Purchase Button
            Button(
                onClick = {
                    onPurchaseVip(if (selectedPlan == "lifetime") "VIP ถาวร (39฿)" else "VIP รายเดือน (19฿)")
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("btn_confirm_purchase_vip"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "ยืนยันเติมเงิน ${if (selectedPlan == "lifetime") "฿39" else "฿19"} (ปิดโฆษณาทันที)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Alternative: Watch video ad for 1-hour VIP
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Transparent)
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .clickable {
                        onWatchAdForTempVip()
                        onDismiss()
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "หรือ ดูโฆษณา 1 ครั้ง เพื่อรับสิทธิ์ VIP ปลอดโฆษณา 1 ชม. ฟรี",
                    color = theme.colors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
