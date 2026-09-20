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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
    var selectedPlan by remember { mutableStateOf("zipspeed_ad_free_lifetime") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF0F1522) else Color.White,
        scrimColor = Color(0x99000000)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("vip_ad_free_modal")
        ) {
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
                            .background(theme.colors.primary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = theme.colors.primary
                        )
                    }
                    Column {
                        Text(
                            text = if (isVipAdFree) "Ad-free เปิดใช้งาน" else "ปิดโฆษณา",
                            color = theme.colors.textMain,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ราคาและสกุลเงินต้องมาจาก Google Play ตามประเทศ",
                            color = theme.colors.textMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.colors.textMuted)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PlanCard(
                    title = "ถาวร",
                    subtitle = "ซื้อครั้งเดียว",
                    selected = selectedPlan == "zipspeed_ad_free_lifetime",
                    onClick = { selectedPlan = "zipspeed_ad_free_lifetime" },
                    modifier = Modifier.weight(1f)
                )
                PlanCard(
                    title = "รายเดือน",
                    subtitle = "Subscription",
                    selected = selectedPlan == "zipspeed_ad_free_monthly",
                    onClick = { selectedPlan = "zipspeed_ad_free_monthly" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.colors.primary.copy(alpha = 0.08f))
                    .border(1.dp, theme.colors.primary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "ชำระผ่าน Google Play เท่านั้น • ห้ามปลดสิทธิ์จากค่าจำลอง • สิทธิ์ต้องยืนยันจากรายการซื้อจริง",
                    color = theme.colors.textMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = {
                    onPurchaseVip(selectedPlan)
                    onDismiss()
                },
                enabled = !isVipAdFree,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_confirm_purchase_vip"),
                colors = ButtonDefaults.buttonColors(containerColor = theme.colors.primary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isVipAdFree) "เปิดใช้งานแล้ว" else "ซื้อผ่าน Google Play",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Rewarded ad จะเปิดใช้เมื่อ AdMob จริงโหลดสำเร็จและได้รับ consent แล้วเท่านั้น",
                color = theme.colors.textMuted,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) theme.colors.primary.copy(alpha = 0.12f) else theme.colors.cardBg)
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) theme.colors.primary else theme.colors.border,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(title, color = theme.colors.textMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = theme.colors.textMuted, fontSize = 10.sp)
        Text("ราคาจาก Play", color = theme.colors.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
