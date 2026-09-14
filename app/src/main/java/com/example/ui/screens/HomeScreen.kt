package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import com.example.engine.NetworkIpInfo
import com.example.model.Language
import com.example.model.MobilePerformanceState
import com.example.model.ServerInfo
import com.example.model.SpeedTestState
import com.example.model.SpeedUnit
import com.example.model.SubTestMode
import com.example.model.TestPhase
import com.example.model.VideoTestState
import com.example.model.WebTestState
import com.example.ui.components.verticalScrollbar
import com.example.ui.components.IpAddressCard
import com.example.ui.components.MetricsGrid
import com.example.ui.components.ServerMapVisualizer
import com.example.ui.components.ServerRowCard
import com.example.ui.components.ShareDetailModal
import com.example.ui.components.ShareReportData
import com.example.ui.components.SpeedGauge
import com.example.ui.components.VideoTestView
import com.example.ui.components.WebTestView
import com.example.ui.theme.CyberInk
import com.example.ui.theme.CyberMuted
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonBlueLight
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRose

@Composable
fun HomeScreen(
    testState: SpeedTestState,
    selectedServer: ServerInfo,
    ipInfo: NetworkIpInfo,
    speedUnit: SpeedUnit,
    language: Language,
    reducedMotion: Boolean,
    isPrecisionMode: Boolean = false,
    videoState: VideoTestState = VideoTestState(),
    webState: WebTestState = WebTestState(),
    onStartTest: () -> Unit,
    onStartPrecisionTest: () -> Unit,
    onCancelTest: () -> Unit,
    onStartVideoTest: () -> Unit = {},
    onCancelVideoTest: () -> Unit = {},
    onStartWebTest: () -> Unit = {},
    onOpenServerModal: () -> Unit,
    onRefreshIp: () -> Unit,
    onToggleSpeedUnit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedTestMode by remember { mutableStateOf(SubTestMode.SPEED) }
    val isRunning = testState.phase != TestPhase.IDLE && testState.phase != TestPhase.COMPLETED && testState.phase != TestPhase.ERROR
    var showShareModal by remember { mutableStateOf(false) }

    // Shimmer button animation
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerShift by if (reducedMotion) {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = -0.5f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(2800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerShift"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .verticalScrollbar(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Eyebrow Title
            Text(
                text = if (language == Language.TH) "ประสิทธิภาพเครือข่าย" else "NETWORK PERFORMANCE",
                color = CyberMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
            )

            // Sub-test Selector Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x18FFFFFF))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val modes = listOf(
                    SubTestMode.SPEED to (if (language == Language.TH) "⚡ สปีด" else "Speed"),
                    SubTestMode.VIDEO to (if (language == Language.TH) "🎬 วิดีโอ" else "Video"),
                    SubTestMode.WEB to (if (language == Language.TH) "🌐 เว็บ" else "Web")
                )
                modes.forEach { (mode, label) ->
                    val isSelected = selectedTestMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) NeonBlue else Color.Transparent)
                            .clickable { selectedTestMode = mode }
                            .padding(vertical = 7.dp)
                            .testTag("mode_tab_${mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else CyberMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTestMode) {
                SubTestMode.VIDEO -> {
                    VideoTestView(
                        videoState = videoState,
                        language = language,
                        onStartTest = onStartVideoTest,
                        onCancelTest = onCancelVideoTest
                    )
                }
                SubTestMode.WEB -> {
                    WebTestView(
                        webState = webState,
                        language = language,
                        onStartTest = onStartWebTest
                    )
                }
                SubTestMode.SPEED -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Speedometer Gauge
                        SpeedGauge(
                            speedValue = testState.liveSpeed,
                            progressFraction = testState.progressFraction,
                            speedUnit = speedUnit,
                            reducedMotion = reducedMotion,
                            isTesting = isRunning,
                            phase = testState.phase,
                            onToggleUnit = onToggleSpeedUnit,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

            // Dynamic Status Text & Smooth Color Transition Animation
            val statusText = when (testState.phase) {
                TestPhase.IDLE -> if (language == Language.TH) "พร้อมทดสอบ" else "Ready to Test"
                TestPhase.TESTING_PING -> if (language == Language.TH) "กำลังวัดค่าปิง..." else "Testing Ping..."
                TestPhase.TESTING_DOWNLOAD -> if (language == Language.TH) "กำลังวัดความเร็วดาวน์โหลด..." else "Testing Download..."
                TestPhase.TESTING_UPLOAD -> if (language == Language.TH) "กำลังวัดความเร็วอัปโหลด..." else "Testing Upload..."
                TestPhase.COMPLETED -> if (language == Language.TH) "ทดสอบเสร็จสิ้น" else "Test Complete"
                TestPhase.ERROR -> testState.errorMessage ?: if (language == Language.TH) "ไม่พบการเชื่อมต่ออินเทอร์เน็ต" else "Connection Failed"
            }

            val targetStatusColor = when (testState.phase) {
                TestPhase.IDLE -> Color(0xFFC7D7FF)
                TestPhase.TESTING_PING -> NeonBlue          // Pulsing blue for ping
                TestPhase.TESTING_DOWNLOAD -> NeonGreen      // Green for download
                TestPhase.TESTING_UPLOAD -> NeonPurple        // Purple for upload
                TestPhase.COMPLETED -> NeonGreen             // Vibrant green for completion
                TestPhase.ERROR -> NeonRose                  // Vibrant red for error
            }

            val animatedStatusColor by animateColorAsState(
                targetValue = targetStatusColor,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                label = "statusColor"
            )

            val statusPulseAlpha by if (isRunning && !reducedMotion) {
                infiniteTransition.animateFloat(
                    initialValue = 0.55f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "statusPulse"
                )
            } else {
                androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1.0f) }
            }

            AnimatedContent(
                targetState = statusText,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "statusTextAnim",
                modifier = Modifier.padding(vertical = 4.dp)
            ) { targetText ->
                Text(
                    text = targetText,
                    color = animatedStatusColor.copy(alpha = statusPulseAlpha),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.4.sp,
                    modifier = Modifier.testTag("test_status_text")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Action Button (START / CANCEL / UPGRADE) - Prominently placed right below gauge
            val buttonText = when {
                isRunning -> if (language == Language.TH) "ยกเลิกการทดสอบ" else "CANCEL TEST"
                testState.phase == TestPhase.ERROR -> if (language == Language.TH) "ลองใหม่อีกครั้ง" else "RETRY TEST"
                testState.phase == TestPhase.COMPLETED -> if (language == Language.TH) "ทดสอบอีกครั้ง" else "TEST AGAIN"
                else -> if (language == Language.TH) "เริ่มทดสอบความเร็ว" else "START SPEED TEST"
            }

            val buttonGradient = when {
                isRunning -> Brush.horizontalGradient(listOf(Color(0xFFE53935), Color(0xFFD32F2F)))
                testState.phase == TestPhase.ERROR -> Brush.horizontalGradient(listOf(NeonRose, Color(0xFFD32F2F)))
                else -> Brush.linearGradient(
                    colors = listOf(NeonBlue, NeonBlueLight, NeonPurple),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(buttonGradient)
                    .border(1.dp, Color(0x3DFFFFFF), RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White)
                    ) {
                        if (isRunning) onCancelTest() else onStartTest()
                    }
                    .testTag("start_test_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buttonText,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }

            if (!isRunning) {
                Spacer(modifier = Modifier.height(8.dp))

                // Precision Test Button (Clears app cache before running speed test)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x228B7CFF))
                        .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = NeonPurple)
                        ) { onStartPrecisionTest() }
                        .testTag("precision_test_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (language == Language.TH)
                            "เทสแบบแม่นยำ (ปิดแอพพื้นหลังก่อน + เคลียร์ cache)"
                        else
                            "Precision Test (Clear Cache First)",
                        color = Color(0xFFE0D7FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Connection Metrics Grid
            MetricsGrid(
                testState = testState,
                speedUnit = speedUnit,
                language = language,
                isProPlan = isProPlan,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (testState.phase == TestPhase.COMPLETED && testState.downloadMbps != null && testState.uploadMbps != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF2F7BFF), Color(0xFF527DFF))
                            )
                        )
                        .border(1.dp, Color(0x3DFFFFFF), RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Color.White)
                        ) {
                            showShareModal = true
                        }
                        .testTag("share_result_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Report",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (language == Language.TH) "แชร์รายละเอียดผลการทดสอบ" else "Share Test Details",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Result Trust Details Panel (Item 1 & 2)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(com.example.ui.theme.CyberPanel)
                    .border(1.dp, Color(0x1F2F7BFF), RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("trust_details_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Connection Type",
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (language == Language.TH)
                                "WiFi 5GHz • AIS Fibre 5G"
                            else
                                "WiFi 5GHz • AIS Fibre 5G",
                            color = Color(0xFF90CAF9),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(NeonGreen)
                        )
                        Text(
                            text = if (language == Language.TH) "สถานะเสถียร" else "Optimal Link",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == Language.TH)
                            "เราท์ติ้ง: ใกล้สุด ${selectedServer.distanceKm}km (${selectedServer.subLocation})"
                        else
                            "Routing: Nearest ${selectedServer.distanceKm}km (${selectedServer.subLocation})",
                        color = CyberMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${selectedServer.basePingMs}ms",
                        color = Color(0xFFA9C6FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Server Selection Card
            ServerRowCard(
                server = selectedServer,
                language = language,
                onOpenServerModal = onOpenServerModal
            )


            Spacer(modifier = Modifier.height(8.dp))

            // Interactive Server Location Radar Map
            ServerMapVisualizer(
                selectedServer = selectedServer,
                language = language,
                onSelectServer = { server ->
                    onOpenServerModal()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // IP Address Card
            IpAddressCard(
                ipInfo = ipInfo,
                language = language,
                onRefreshIp = onRefreshIp
            )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    if (showShareModal && testState.downloadMbps != null && testState.uploadMbps != null) {
        ShareDetailModal(
            reportData = ShareReportData(
                downloadMbps = testState.downloadMbps,
                uploadMbps = testState.uploadMbps,
                pingMs = testState.pingMs ?: selectedServer.basePingMs,
                jitterMs = testState.jitterMs ?: 2,
                packetLossPercent = testState.packetLossPercent ?: 0.0,
                serverName = selectedServer.name,
                networkType = "WiFi 5GHz",
                publicIp = ipInfo.publicIp,
                ispName = ipInfo.ispName
            ),
            speedUnit = speedUnit,
            language = language,
            onDismiss = { showShareModal = false }
        )
    }
}
