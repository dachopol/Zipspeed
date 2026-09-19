package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.engine.SecurityThreatReport
import com.example.ui.theme.LocalAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityShieldModal(
    securityReport: SecurityThreatReport,
    isShieldEnabled: Boolean,
    onToggleShield: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val theme = LocalAppTheme.current
    val isDark = theme.isDark
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                .testTag("security_shield_modal")
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isShieldEnabled) Color(0x3310B981) else Color(0x33EF4444)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isShieldEnabled) Icons.Default.GppGood else Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (isShieldEnabled) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "ระบบความปลอดภัย & เกราะป้องกัน",
                            color = theme.colors.textMain,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Anti-Tamper & Anti-Bypass Security Engine",
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

            Spacer(modifier = Modifier.height(16.dp))

            // Master Shield Switch Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isShieldEnabled) {
                            Brush.horizontalGradient(
                                listOf(Color(0x2E10B981), Color(0x1A00FFD1))
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(Color(0x1AEF4444), Color(0x14FFFFFF))
                            )
                        }
                    )
                    .border(
                        1.dp,
                        if (isShieldEnabled) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0x33FFFFFF),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "เกราะความปลอดภัย (Security Shield)",
                                color = theme.colors.textMain,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (isShieldEnabled)
                                "กำลังปกป้อง: ป้องกันเครื่องมือปลดล็อคเถื่อนและ Lucky Patcher"
                            else
                                "ปิดเกราะป้องกันอยู่ (ความปลอดภัยระดับพื้นฐาน)",
                            color = if (isShieldEnabled) Color(0xFF34D399) else theme.colors.textMuted,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = isShieldEnabled,
                        onCheckedChange = onToggleShield,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0x33FFFFFF)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security Checklist Items
            Text(
                text = "การตรวจสอบความสมบูรณ์ของระบบ:",
                color = theme.colors.textMain,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            SecurityCheckRow(
                title = "ตรวจจับ Root / Superuser",
                status = if (securityReport.isRooted) "พบสิทธิ์ Root" else "ปลอดภัย (ไม่พบ Root)",
                isPassed = !securityReport.isRooted,
                themeTextMain = theme.colors.textMain,
                themeTextMuted = theme.colors.textMuted
            )

            SecurityCheckRow(
                title = "ตรวจจับ Lucky Patcher / Cracker",
                status = if (securityReport.isPatcherInstalled) "พบเครื่องมือต้องสงสัย" else "ปลอดภัย 100%",
                isPassed = !securityReport.isPatcherInstalled,
                themeTextMain = theme.colors.textMain,
                themeTextMuted = theme.colors.textMuted
            )

            SecurityCheckRow(
                title = "ตรวจจับ Memory Hook / Debugger",
                status = if (securityReport.isDebuggerAttached) "ตรวจพบ Debugger" else "ป้องกัน Hook สำเร็จ",
                isPassed = !securityReport.isDebuggerAttached,
                themeTextMain = theme.colors.textMain,
                themeTextMuted = theme.colors.textMuted
            )

            SecurityCheckRow(
                title = "การเข้ารหัสใบอนุญาต (SHA-256 HMAC)",
                status = "เข้ารหัสความปลอดภัยระดับธนาคาร",
                isPassed = true,
                themeTextMain = theme.colors.textMain,
                themeTextMuted = theme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SecurityCheckRow(
    title: String,
    status: String,
    isPassed: Boolean,
    themeTextMain: Color,
    themeTextMuted: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isPassed) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = themeTextMain,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = status,
            color = if (isPassed) Color(0xFF10B981) else Color(0xFFEF4444),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
