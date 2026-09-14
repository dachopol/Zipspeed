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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import com.example.model.SpeedTestState
import com.example.model.SpeedUnit
import com.example.ui.theme.CyberInk
import com.example.ui.theme.CyberMuted
import com.example.ui.theme.CyberPanel
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import java.util.Locale

@Composable
fun MetricsGrid(
    testState: SpeedTestState,
    speedUnit: SpeedUnit,
    language: Language,
    isProPlan: Boolean,
    modifier: Modifier = Modifier
) {
    val pingText = testState.pingMs?.let { "$it ms" } ?: "—"

    val downloadVal = testState.downloadMbps?.let {
        if (speedUnit == SpeedUnit.MB_S) it / 8.0 else it
    }
    val downloadText = downloadVal?.let { String.format(Locale.US, "%.1f", it) } ?: "—"

    val uploadVal = testState.uploadMbps?.let {
        if (speedUnit == SpeedUnit.MB_S) it / 8.0 else it
    }
    val uploadText = uploadVal?.let { String.format(Locale.US, "%.1f", it) } ?: "—"

    val pingLabel = if (language == Language.TH) "ปิง" else "PING"
    val downloadLabel = if (language == Language.TH) "ดาวน์โหลด" else "DOWNLOAD"
    val uploadLabel = if (language == Language.TH) "อัปโหลด" else "UPLOAD"

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // PING CARD
            MetricCard(
                label = pingLabel,
                value = pingText,
                accentColor = NeonBlue,
                modifier = Modifier.weight(1f).testTag("metric_ping")
            )

            // DOWNLOAD CARD
            MetricCard(
                label = downloadLabel,
                value = downloadText,
                accentColor = NeonGreen,
                modifier = Modifier.weight(1f).testTag("metric_download")
            )

            // UPLOAD CARD
            MetricCard(
                label = uploadLabel,
                value = uploadText,
                accentColor = NeonPurple,
                modifier = Modifier.weight(1f).testTag("metric_upload")
            )
        }

        if (isProPlan && (testState.jitterMs != null || testState.packetLossPercent != null)) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    label = if (language == Language.TH) "จิทเทอร์ (JITTER)" else "JITTER",
                    value = testState.jitterMs?.let { "$it ms" } ?: "—",
                    accentColor = Color(0xFFFFB74D),
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    label = if (language == Language.TH) "การสูญเสียแพ็กเก็ต" else "PACKET LOSS",
                    value = testState.packetLossPercent?.let { String.format(Locale.US, "%.1f%%", it) } ?: "0.0%",
                    accentColor = Color(0xFFFF5252),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Real File Download Speed Conversion Banner (Mbps vs MB/s calculation)
        testState.downloadMbps?.let { dlMbps ->
            val realFileSpeedMBs = dlMbps / 8.0
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x2210B981))
                    .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("real_file_download_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == Language.TH) "โหลดไฟล์จริง (Real Download Speed):" else "Actual File Download Speed:",
                        color = NeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f MB/s", realFileSpeedMBs),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (language == Language.TH)
                        "(${String.format(Locale.US, "%.1f", dlMbps)} Mbps ÷ 8 = โหลดไฟล์จริง ${String.format(Locale.US, "%.2f", realFileSpeedMBs)} MB/s ไม่โอเวอร์)"
                    else
                        "(${String.format(Locale.US, "%.1f", dlMbps)} Mbps ÷ 8 = ${String.format(Locale.US, "%.2f", realFileSpeedMBs)} MB/s actual file speed)",
                    color = CyberMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CyberPanel)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(accentColor.copy(alpha = 0.5f), Color(0x14FFFFFF))
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(accentColor)
            )
            Text(
                text = label,
                color = CyberMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = CyberInk,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.2).sp
        )
    }
}
