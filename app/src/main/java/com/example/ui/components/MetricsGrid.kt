package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.SpeedTestState
import com.example.model.SpeedUnit
import com.example.ui.theme.LocalAppTheme
import java.util.Locale

@Composable
fun MetricsGrid(
    testState: SpeedTestState,
    speedUnit: SpeedUnit,
    language: Language,
    isProPlan: Boolean = true,
    modifier: Modifier = Modifier
) {
    val downloadVal = testState.downloadMbps?.let {
        if (speedUnit == SpeedUnit.MB_S) it / 8.0 else it
    }
    val downloadText = downloadVal?.let { String.format(Locale.US, "%.1f", it) } ?: "193"

    val uploadVal = testState.uploadMbps?.let {
        if (speedUnit == SpeedUnit.MB_S) it / 8.0 else it
    }
    val uploadText = uploadVal?.let { String.format(Locale.US, "%.1f", it) } ?: "68.4"

    val pingText = testState.pingMs?.toString() ?: "39.0"
    val jitterText = testState.jitterMs?.toString() ?: "8.6"

    val statDownloadColor = Color(0xFF34C759)
    val statUploadColor = Color(0xFFAF52DE)
    val statPingColor = Color(0xFFFF9500)
    val statJitterColor = Color(0xFF007AFF)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("metrics_grid"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Download & Upload (Symmetrical 1:1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SymmetricalStatCard(
                name = "Download (${speedUnit.label})",
                value = downloadText,
                unit = speedUnit.label,
                icon = Icons.Default.ArrowDownward,
                accentColor = statDownloadColor,
                sparklinePattern = listOf(0.7f, 0.45f, 0.55f, 0.35f, 0.6f, 0.5f, 0.7f),
                modifier = Modifier
                    .weight(1f)
                    .testTag("stat_download_card")
            )

            SymmetricalStatCard(
                name = "Upload (${speedUnit.label})",
                value = uploadText,
                unit = speedUnit.label,
                icon = Icons.Default.ArrowUpward,
                accentColor = statUploadColor,
                sparklinePattern = listOf(0.5f, 0.6f, 0.45f, 0.7f, 0.55f, 0.4f, 0.6f),
                modifier = Modifier
                    .weight(1f)
                    .testTag("stat_upload_card")
            )
        }

        // Row 2: Ping & Jitter (Symmetrical 1:1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SymmetricalStatCard(
                name = "Ping (ms)",
                value = pingText,
                unit = "ms",
                icon = Icons.Default.SportsTennis,
                accentColor = statPingColor,
                sparklinePattern = listOf(0.8f, 0.65f, 0.75f, 0.85f, 0.65f, 0.8f, 0.7f),
                modifier = Modifier
                    .weight(1f)
                    .testTag("stat_ping_card")
            )

            SymmetricalStatCard(
                name = "Jitter (ms)",
                value = jitterText,
                unit = "ms",
                icon = Icons.Default.Timeline,
                accentColor = statJitterColor,
                sparklinePattern = listOf(0.6f, 0.7f, 0.55f, 0.45f, 0.65f, 0.5f, 0.75f),
                modifier = Modifier
                    .weight(1f)
                    .testTag("stat_jitter_card")
            )
        }
    }
}

@Composable
private fun SymmetricalStatCard(
    name: String,
    value: String,
    unit: String,
    icon: ImageVector,
    accentColor: Color,
    sparklinePattern: List<Float>,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val isDark = theme.isDark

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isDark) Color(0xFF131824).copy(alpha = 0.85f) else Color(0xFFFFFFFF)
            )
            .border(
                width = 1.dp,
                color = if (isDark) Color(0x33FFFFFF) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 0.dp, start = 14.dp, end = 14.dp, bottom = 12.dp)
        ) {
            // Top Accent Bar (Matches HTML .stat-card::before)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(accentColor)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stat Header: Icon + Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = name,
                    color = theme.colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Stat Body: Value + Unit
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    color = theme.colors.textMain,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = unit,
                    color = theme.colors.textMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sparkline Wave Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            ) {
                if (sparklinePattern.size < 2) return@Canvas

                val width = size.width
                val height = size.height
                val stepX = width / (sparklinePattern.size - 1)

                val path = Path().apply {
                    moveTo(0f, sparklinePattern[0] * height)
                    for (i in 1 until sparklinePattern.size) {
                        val prevX = (i - 1) * stepX
                        val prevY = sparklinePattern[i - 1] * height
                        val curX = i * stepX
                        val curY = sparklinePattern[i] * height
                        val cX = (prevX + curX) / 2f
                        cubicTo(cX, prevY, cX, curY, curX, curY)
                    }
                }

                // Subtle gradient fill under sparkline
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        listOf(accentColor.copy(alpha = 0.25f), Color.Transparent)
                    )
                )

                drawPath(
                    path = path,
                    color = accentColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}
