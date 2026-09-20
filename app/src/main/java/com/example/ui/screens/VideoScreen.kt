package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.model.VideoResolution
import com.example.model.VideoTestState
import com.example.ui.components.VideoTestView
import com.example.ui.components.verticalScrollbar
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.WinePink

@Composable
fun VideoScreen(
    videoState: VideoTestState,
    language: Language,
    onStartTest: () -> Unit,
    onCancelTest: () -> Unit,
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
            .testTag("video_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.colors.cardBg)
                    .border(1.dp, theme.colors.border, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ChampagneGold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = ChampagneGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTh) "ทดสอบคุณภาพสตรีมมิ่งวิดีโอ" else "Video Streaming Quality",
                            color = theme.colors.textMain,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isTh) "ประเมินความละเอียดจาก throughput HTTP ที่วัดได้" else "Estimates resolution from measured HTTP throughput",
                            color = theme.colors.textMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Interactive Video Test View
            VideoTestView(
                videoState = videoState,
                language = language,
                onStartTest = onStartTest,
                onCancelTest = onCancelTest,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Resolution Capability Matrix
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.colors.cardBg)
                    .border(1.dp, theme.colors.border, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isTh) "ตารางผลการรองรับความละเอียด (Resolution Matrix)" else "Resolution Support Matrix",
                        color = theme.colors.textMain,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val resolutions = listOf(
                        VideoResolution.UHD_4K to "4K Ultra HD (2160p)",
                        VideoResolution.QHD_1440P to "2K Quad HD (1440p)",
                        VideoResolution.FHD_1080P to "Full HD (1080p)",
                        VideoResolution.HD_720P to "HD Ready (720p)",
                        VideoResolution.SD_480P to "SD (480p)"
                    )

                    resolutions.forEach { (res, label) ->
                        val reqBandwidth = "${res.minMbps} Mbps+"
                        val isCurrent = videoState.isTesting && videoState.currentResolution == res
                        val isPassed = videoState.isCompleted && (videoState.maxResolutionPassed?.let { it.ordinal >= res.ordinal } ?: false)
                        val isFailed = videoState.isCompleted && !isPassed

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCurrent) ChampagneGold.copy(alpha = 0.1f) else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isCurrent -> ChampagneGold
                                                isPassed -> StatusGreen
                                                isFailed -> Color.Gray.copy(alpha = 0.4f)
                                                else -> theme.colors.border
                                            }
                                        )
                                )
                                Text(
                                    text = label,
                                    color = if (isCurrent) ChampagneGold else theme.colors.textMain,
                                    fontSize = 12.sp,
                                    fontWeight = if (isCurrent || isPassed) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                            Text(
                                text = when {
                                    isCurrent -> if (isTh) "กำลังทดสอบ..." else "Testing..."
                                    isPassed -> if (isTh) "ผ่านฉลุย" else "Supported"
                                    isFailed -> if (isTh) "ไม่แนะนำ" else "Not recommended"
                                    else -> reqBandwidth
                                },
                                color = when {
                                    isCurrent -> ChampagneGold
                                    isPassed -> StatusGreen
                                    isFailed -> theme.colors.textMuted
                                    else -> theme.colors.textMuted
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Method note: this is a throughput-based estimate, not a platform certification.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.colors.cardBg)
                    .border(1.dp, theme.colors.border, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isTh) "วิธีประเมิน" else "Method",
                        color = theme.colors.textMain,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isTh)
                            "ผลนี้อิง throughput HTTP ที่ Zipspeed วัดได้และเกณฑ์ภายในของแอป ไม่ใช่การเล่นวิดีโอจริง และไม่ใช่การรับรองจาก YouTube, Netflix หรือแพลตฟอร์มอื่น"
                        else
                            "This result is based on measured HTTP throughput and Zipspeed's internal thresholds. It is not real video playback and is not certification from YouTube, Netflix, or any other platform.",
                        color = theme.colors.textMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    if (videoState.isCompleted) {
                        Text(
                            text = if (isTh)
                                "Throughput ล่าสุด: ${String.format("%.1f", videoState.streamBitrateMbps)} Mbps"
                            else
                                "Latest throughput: ${String.format("%.1f", videoState.streamBitrateMbps)} Mbps",
                            color = ChampagneGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}
