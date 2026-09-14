package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.VideoResolution
import com.example.model.VideoTestState
import com.example.model.WebTestState

import com.example.ui.theme.CyberMuted
import com.example.ui.theme.CyberPanel

import com.example.ui.theme.CyberSurface
import com.example.ui.theme.GoldPro
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRose

// -------------------------------------------------------------
// 1. VIDEO STREAMING QUALITY TEST VIEW
// -------------------------------------------------------------
@Composable
fun VideoTestView(
    videoState: VideoTestState,
    language: Language,
    onStartTest: () -> Unit,
    onCancelTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "video_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_test_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Video Preview Simulated Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF131D28), Color(0xFF0F151E))
                    )
                )
                .border(1.dp, NeonBlue.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = if (videoState.isTesting) NeonBlue else CyberMuted,
                    modifier = Modifier
                        .size(44.dp)
                        .scale(if (videoState.isTesting) pulseScale else 1f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (videoState.isTesting)
                        "กำลังทดสอบ: ${videoState.currentResolution.label}"
                    else if (videoState.isCompleted)
                        "ผลการทดสอบ: รองรับสูงสุด ${videoState.maxResolutionPassed?.label ?: "ไม่ผ่าน (เครือข่ายช้ามาก)"}"
                    else
                        stringResource(R.string.str_ready_to_test_video_streaming_22),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                if (videoState.isTesting) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bitrate: ${String.format("%.1f", videoState.streamBitrateMbps)} Mbps | Buffer: ${videoState.bufferTimeMs} ms",
                        color = NeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Top resolution badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x33000000))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = videoState.currentResolution.label,
                    color = if (videoState.currentResolution == VideoResolution.UHD_4K) GoldPro else NeonBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Bottom Progress bar when testing
            if (videoState.isTesting) {
                LinearProgressIndicator(
                    progress = { videoState.progress },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(4.dp),
                    color = NeonBlue,
                    trackColor = Color(0x22FFFFFF)
                )
            }
        }

        // Action Button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (videoState.isTesting) {
                Button(
                    onClick = onCancelTest,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRose),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_cancel_video_test")
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.str_cancel_test_23),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onStartTest,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_start_video_test")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.str_start_video_test_24),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Supported Resolutions Grid
        Text(
            text = stringResource(R.string.str_resolution_support_25),
            color = CyberMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val resolutions = listOf(
                VideoResolution.UHD_4K,
                VideoResolution.QHD_1440P,
                VideoResolution.FHD_1080P,
                VideoResolution.HD_720P,
                VideoResolution.SD_480P
            )

            resolutions.forEach { res ->
                val isPassed = videoState.isCompleted && (videoState.maxResolutionPassed != null && videoState.maxResolutionPassed.ordinal >= res.ordinal)
                val isCurrentlyTesting = videoState.isTesting && videoState.currentResolution == res

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isPassed) NeonGreen.copy(alpha = 0.12f)
                            else if (isCurrentlyTesting) NeonBlue.copy(alpha = 0.15f)
                            else CyberSurface
                        )
                        .border(
                            1.dp,
                            if (isPassed) NeonGreen.copy(alpha = 0.4f)
                            else if (isCurrentlyTesting) NeonBlue.copy(alpha = 0.6f)
                            else Color(0x18FFFFFF),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (res == VideoResolution.UHD_4K) GoldPro.copy(alpha = 0.2f) else Color(0x15FFFFFF))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = res.label,
                                color = if (res == VideoResolution.UHD_4K) GoldPro else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = res.badgeText,
                            color = CyberMuted,
                            fontSize = 12.sp
                        )
                    }

                    if (isPassed) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Passed",
                                tint = NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.str_pass_smooth_26),
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (isCurrentlyTesting) {
                        Text(
                            text = "กำลังทดสอบ...",
                            color = NeonBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = "ต้องการ ${res.minMbps.toInt()} Mbps",
                            color = Color(0xB3FFFFFF),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. WEB BROWSING & CDN PERFORMANCE VIEW
// -------------------------------------------------------------
@Composable
fun WebTestView(
    webState: WebTestState,
    language: Language,
    onStartTest: () -> Unit,
    onCancelTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("web_test_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Overall Web Browsing Score Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF14202E), Color(0xFF0F151E))
                    )
                )
                .border(1.dp, NeonBlue.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.str_web_browsing_score_27),
                        color = CyberMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (webState.isCompleted) "${webState.overallScore}/100" else "--/100",
                        color = if (webState.isCompleted) NeonGreen else Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (webState.isCompleted)
                            (if (webState.overallScore >= 90) "เกรด A+ (โหลดเร็วเป็นพิเศษ)" else "เกรด A (เร็วปกติ)")
                        else if (webState.isTesting)
                            "กำลังวัดความเร็วเปิดหน้าเว็บและ CDN..."
                        else
                            "พร้อมทดสอบความเร็วเปิดหน้าเว็บ",
                        color = CyberMuted,
                        fontSize = 12.sp
                    )
                }

                if (webState.isTesting) {
                    Button(
                        onClick = onCancelTest,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRose),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_cancel_web_test")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.str_cancel_test_23),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Button(
                        onClick = onStartTest,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_start_web_test")
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "เริ่มทดสอบ",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // List of websites tested
        Text(
            text = stringResource(R.string.str_tested_websites_cdns_28),
            color = CyberMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            webState.sites.forEach { site ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberSurface)
                        .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = site.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${site.category} (${site.domain})",
                            color = Color(0xB3FFFFFF),
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (site.latencyMs > 0) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${site.latencyMs} ms",
                                    color = if (site.latencyMs < 80) NeonGreen else NeonAmber,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "TTFB: ${site.ttfbMs} ms",
                                    color = CyberMuted,
                                    fontSize = 10.sp
                                )
                            }
                        } else {
                            Text(
                                text = site.statusText,
                                color = Color(0xB3FFFFFF),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
