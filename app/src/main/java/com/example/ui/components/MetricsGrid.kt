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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.SpeedTestState
import com.example.model.SpeedUnit
import com.example.model.TestPhase
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.WinePink
import java.util.Locale

@Composable
fun MetricsGrid(
    testState: SpeedTestState,
    speedUnit: SpeedUnit,
    language: Language,
    isProPlan: Boolean = true,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val isTh = language == Language.TH

    val downloadVal = testState.downloadMbps?.let {
        if (speedUnit == SpeedUnit.MB_S) it / 8.0 else it
    }
    val downloadText = downloadVal?.let { String.format(Locale.US, "%.1f", it) } ?: "--"

    val uploadVal = testState.uploadMbps?.let {
        if (speedUnit == SpeedUnit.MB_S) it / 8.0 else it
    }
    val uploadText = uploadVal?.let { String.format(Locale.US, "%.1f", it) } ?: "--"

    val pingText = testState.pingMs?.toString() ?: "--"
    val jitterText = testState.jitterMs?.toString() ?: "--"

    // Refined Palette (Champagne, Wine, Green, Muted Slate)
    val downloadAccent = ChampagneGold
    val uploadAccent = WinePink
    val pingAccent = ChampagneGold
    val jitterAccent = theme.colors.textMuted

    // Convert raw Mbps samples to normalized 0f..1f for canvas sparkline
    fun normalizeSamples(samples: List<Double>): List<Float> {
        if (samples.size < 2) return emptyList()
        val min = samples.minOrNull() ?: 0.0
        val max = samples.maxOrNull() ?: 1.0
        val range = (max - min).coerceAtLeast(0.1)
        return samples.map { ((it - min) / range).toFloat().coerceIn(0.05f, 0.95f) }
    }

    val dlSparkline = normalizeSamples(testState.downloadSamples)
    val ulSparkline = normalizeSamples(testState.uploadSamples)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("metrics_grid"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Optional Warning Banner if unstable
        if (!testState.warningMessage.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFFF3CD).copy(alpha = if (theme.isDark) 0.12f else 0.90f))
                    .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = testState.warningMessage,
                    color = if (theme.isDark) Color(0xFFFFE082) else Color(0xFF856404),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.sp
                )
            }
        }

        // Row 1: Download & Upload (Most Prominent Metrics)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProminentStatCard(
                name = if (isTh) "ดาวน์โหลด" else "Download",
                unit = speedUnit.label,
                value = downloadText,
                icon = Icons.Default.ArrowDownward,
                accentColor = downloadAccent,
                sparklinePattern = dlSparkline,
                isLive = testState.phase == TestPhase.TESTING_DOWNLOAD,
                isProminent = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("stat_download_card")
            )

            ProminentStatCard(
                name = if (isTh) "อัปโหลด" else "Upload",
                unit = speedUnit.label,
                value = uploadText,
                icon = Icons.Default.ArrowUpward,
                accentColor = uploadAccent,
                sparklinePattern = ulSparkline,
                isLive = testState.phase == TestPhase.TESTING_UPLOAD,
                isProminent = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("stat_upload_card")
            )
        }

        // Row 2: Ping & Jitter (Matching visual style in the next row)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProminentStatCard(
                name = if (isTh) "HTTP Latency" else "HTTP Latency",
                subTitle = if (isTh) "RTT" else "RTT",
                unit = "ms",
                value = pingText,
                icon = Icons.Default.Speed,
                accentColor = pingAccent,
                sparklinePattern = emptyList(),
                isLive = testState.phase == TestPhase.TESTING_PING,
                isProminent = false,
                modifier = Modifier
                    .weight(1f)
                    .testTag("stat_ping_card")
            )

            ProminentStatCard(
                name = if (isTh) "Jitter" else "Jitter",
                subTitle = if (isTh) "ความนิ่ง" else "Variance",
                unit = "ms",
                value = jitterText,
                icon = Icons.Default.Timeline,
                accentColor = jitterAccent,
                sparklinePattern = emptyList(),
                isLive = false,
                isProminent = false,
                modifier = Modifier
                    .weight(1f)
                    .testTag("stat_jitter_card")
            )
        }

        // Real Measurement Formula & Explanation Note
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = theme.colors.textMuted.copy(alpha = 0.6f),
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = if (isTh) {
                    "วัดผลจริงผ่าน Edge Node • สูตร: Mbps = (จำนวนไบต์ที่รับส่งจริง × 8) ÷ (วินาที × 1,000,000)"
                } else {
                    "Measured via Edge Node • Formula: Mbps = (Real Bytes × 8) ÷ (Seconds × 1,000,000)"
                },
                color = theme.colors.textMuted.copy(alpha = 0.65f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun ProminentStatCard(
    name: String,
    value: String,
    unit: String,
    icon: ImageVector,
    accentColor: Color,
    sparklinePattern: List<Float>,
    modifier: Modifier = Modifier,
    subTitle: String? = null,
    isLive: Boolean = false,
    isProminent: Boolean = false
) {
    val theme = LocalAppTheme.current
    val isDark = theme.isDark

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(theme.colors.cardBg)
            .border(
                width = if (isLive) 1.5.dp else 1.dp,
                color = if (isLive) accentColor else theme.colors.border,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = if (isProminent) 14.dp else 12.dp,
                    start = 14.dp,
                    end = 14.dp,
                    bottom = if (isProminent) 14.dp else 12.dp
                )
        ) {
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
                    fontSize = if (isProminent) 12.sp else 11.sp,
                    fontWeight = FontWeight.Medium
                )
                if (subTitle != null) {
                    Text(
                        text = "• $subTitle",
                        color = theme.colors.textMuted.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stat Body: Value + Unit (Tabular figures)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    color = if (isLive) accentColor else theme.colors.textMain,
                    fontSize = if (isProminent) 26.sp else 20.sp,
                    fontWeight = FontWeight.SemiBold, // 600
                    fontFamily = FontFamily.Monospace, // Tabular numerals
                    letterSpacing = (-0.5).sp
                )
                if (value != "--") {
                    Text(
                        text = unit,
                        color = theme.colors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            // Sparkline for prominent metrics (Download / Upload)
            if (isProminent) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                ) {
                    if (sparklinePattern.size >= 2) {
                        Canvas(modifier = Modifier.fillMaxWidth().height(18.dp)) {
                            val width = size.width
                            val height = size.height
                            val stepX = width / (sparklinePattern.size - 1)

                            val path = Path().apply {
                                moveTo(0f, (1f - sparklinePattern[0]) * height)
                                for (i in 1 until sparklinePattern.size) {
                                    val prevX = (i - 1) * stepX
                                    val prevY = (1f - sparklinePattern[i - 1]) * height
                                    val curX = i * stepX
                                    val curY = (1f - sparklinePattern[i]) * height
                                    val cX = (prevX + curX) / 2f
                                    cubicTo(cX, prevY, cX, curY, curX, curY)
                                }
                            }

                            drawPath(
                                path = path,
                                color = accentColor.copy(alpha = 0.85f),
                                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    } else {
                        // Subtle baseline
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .align(Alignment.Center)
                                .background(if (isDark) Color(0x18FFFFFF) else Color(0x12000000))
                        )
                    }
                }
            }
        }
    }
}
