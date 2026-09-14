package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.ui.theme.CyberInk
import com.example.ui.theme.CyberMuted
import com.example.ui.theme.GoldPro
import com.example.ui.theme.NeonGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeModal(
    isProPlan: Boolean,
    language: Language,
    onDismiss: () -> Unit,
    onTogglePro: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141320),
        scrimColor = Color(0x99000000)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 12.dp)
                .testTag("upgrade_modal")
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFD700)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Pro Icon",
                            tint = GoldPro,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = if (language == Language.TH) "Zipspeed PRO" else "Zipspeed PRO",
                        color = CyberInk,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = CyberMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (language == Language.TH)
                    "ปลดล็อกฟีเจอร์ระดับพรีเมียมสำหรับการทดสอบเครือข่ายความเร็วสูง"
                else
                    "Unlock premium capabilities for high-throughput network diagnostics.",
                color = CyberMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Pro Benefits list
            val benefits = if (language == Language.TH) listOf(
                "รองรับ Multi-Thread Bandwidth Test สูงสุด 10 Gbps",
                "วิเคราะห์ค่า Jitter & Packet Loss อย่างละเอียด",
                "เลือกระบบทดสอบเซิร์ฟเวอร์อัตโนมัติ (Auto Latency Routing)",
                "ส่งออกประวัติการทดสอบเป็นไฟล์ CSV/JSON"
            ) else listOf(
                "Multi-Thread Bandwidth Engine up to 10 Gbps",
                "Detailed Jitter & Packet Loss Diagnostics",
                "Smart Server Latency Routing",
                "Export Test History to CSV/JSON"
            )

            benefits.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = item,
                        color = CyberInk,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val btnText = if (isProPlan) {
                if (language == Language.TH) "สลับเป็น FREE PLAN" else "SWITCH TO FREE PLAN"
            } else {
                if (language == Language.TH) "อัปเกรดเป็น PRO PLAN" else "UPGRADE TO PRO PLAN"
            }

            val btnBg = if (isProPlan) {
                Brush.horizontalGradient(listOf(Color(0x33FFFFFF), Color(0x22FFFFFF)))
            } else {
                Brush.horizontalGradient(listOf(NeonGreen, Color(0xFF15B884)))
            }

            Button(
                onClick = {
                    onTogglePro()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("confirm_upgrade_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(btnBg)
                        .border(
                            1.dp,
                            if (isProPlan) Color(0x33FFFFFF) else NeonGreen,
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = btnText,
                        color = if (isProPlan) CyberInk else Color(0xFF071510),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
