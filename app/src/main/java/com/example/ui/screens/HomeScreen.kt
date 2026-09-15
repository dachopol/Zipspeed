package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.NetworkIpInfo
import com.example.model.Language
import com.example.model.ServerInfo
import com.example.model.SpeedTestState
import com.example.model.SpeedUnit
import com.example.model.TestPhase
import com.example.model.VideoTestState
import com.example.model.WebTestState
import com.example.ui.components.AdMobBannerView
import com.example.ui.components.ConnectionInfoCard
import com.example.ui.components.ExperienceAssessmentView
import com.example.ui.components.MetricsGrid
import com.example.ui.components.ShareAdCountdownModal
import com.example.ui.components.ShareDetailModal
import com.example.ui.components.ShareReportData
import com.example.ui.components.SpeedGauge
import com.example.ui.components.verticalScrollbar
import com.example.ui.theme.LocalAppTheme

@Composable
fun HomeScreen(
    testState: SpeedTestState,
    selectedServer: ServerInfo,
    ipInfo: NetworkIpInfo,
    speedUnit: SpeedUnit,
    language: Language,
    reducedMotion: Boolean,
    isPrecisionMode: Boolean = false,
    isVipAdFree: Boolean = false,
    isGpsActive: Boolean = true,
    videoState: VideoTestState = VideoTestState(),
    webState: WebTestState = WebTestState(),
    onStartTest: () -> Unit,
    onStartPrecisionTest: () -> Unit,
    onCancelTest: () -> Unit,
    onStartVideoTest: () -> Unit = {},
    onCancelVideoTest: () -> Unit = {},
    onStartWebTest: () -> Unit = {},
    onCancelWebTest: () -> Unit = {},
    onOpenServerModal: () -> Unit,
    onRefreshIp: () -> Unit,
    onToggleSpeedUnit: () -> Unit = {},
    onToggleGpsMode: () -> Unit = {},
    onOpenVipModal: () -> Unit = {},
    onNavigateToResults: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isRunning = testState.phase != TestPhase.IDLE &&
            testState.phase != TestPhase.COMPLETED &&
            testState.phase != TestPhase.ERROR

    // Sharing flow states: 5-second countdown ad for non-VIP, then detailed share sheet
    var showShareAdCountdownModal by remember { mutableStateOf(false) }
    var showShareDetailModal by remember { mutableStateOf(false) }

    val theme = LocalAppTheme.current
    val isDark = theme.isDark

    // Permission launcher for live GPS mode
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            onToggleGpsMode()
        }
    }

    val handleInitiateShare = {
        if (isVipAdFree) {
            // VIP users bypass the 5s ad immediately!
            showShareDetailModal = true
        } else {
            // Free users must watch the 5-second countdown ad before skipping to share
            showShareAdCountdownModal = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .verticalScrollbar(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // =========================================================================
        // 1. Symmetrical Top Network & ISP Status Card
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) Color(0xFF131824).copy(alpha = 0.85f) else Color(0xFFFFFFFF))
                .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                .padding(14.dp)
                .testTag("network_info_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ISP Title & Brand Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF34C759).copy(alpha = 0.20f))
                                .border(1.dp, Color(0xFF34C759), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ISP",
                                color = Color(0xFF34C759),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = if (ipInfo.ispName.isNotBlank()) ipInfo.ispName else "AIS Fibre Thailand",
                            color = theme.colors.textMain,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Auto Server Selector Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0x18FFFFFF) else Color(0xFFF1F5F9))
                            .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                            .clickable { onOpenServerModal() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Change Server",
                            tint = Color(0xFF00FFD1),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Auto (${selectedServer.name.substringBefore(" ")})",
                            color = theme.colors.textMain,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Server Location, Ping & GPS Mode Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (ipInfo.regionTag.isNotBlank()) ipInfo.regionTag else "Samut Sakhon (12km) • 10ms",
                        color = theme.colors.textMuted,
                        fontSize = 11.sp
                    )

                    // GPS Toggle Button (Requests permission and fetches live coordinates)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isGpsActive) Color(0xFF00FFD1).copy(alpha = 0.15f) else Color(0x14FFFFFF)
                            )
                            .border(
                                1.dp,
                                if (isGpsActive) Color(0xFF00FFD1).copy(alpha = 0.40f) else Color(0x22FFFFFF),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("gps_toggle_btn"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isGpsActive) Icons.Default.GpsFixed else Icons.Default.MyLocation,
                            contentDescription = "GPS Mode",
                            tint = if (isGpsActive) Color(0xFF00FFD1) else theme.colors.textMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isGpsActive) "GPS: ON" else "เปิดโหมด GPS",
                            color = if (isGpsActive) Color(0xFF00FFD1) else theme.colors.textMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // =========================================================================
        // 2. Mathematically Symmetrical Speedometer Gauge with Post-Test AdMob Overlay
        // =========================================================================
        SpeedGauge(
            speedValue = testState.liveSpeed,
            progressFraction = testState.progressFraction,
            speedUnit = speedUnit,
            reducedMotion = reducedMotion,
            isTesting = isRunning,
            phase = testState.phase,
            isVipAdFree = isVipAdFree,
            onOpenVipModal = onOpenVipModal,
            onToggleUnit = onToggleSpeedUnit,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // =========================================================================
        // 3. Symmetrical Dual Action Buttons
        // =========================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Primary Test Button (Magenta Gradient)
            val primaryButtonText = when {
                isRunning -> if (language == Language.TH) "ยกเลิก" else "Cancel"
                testState.phase == TestPhase.COMPLETED -> if (language == Language.TH) "ทดสอบอีกครั้ง" else "Test Again"
                else -> if (language == Language.TH) "เริ่มทดสอบ" else "Start Test"
            }
            val primaryGradient = if (isRunning) {
                Brush.horizontalGradient(listOf(Color(0xFFE53935), Color(0xFFD32F2F)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFE91E63), Color(0xFFFF4081)))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(primaryGradient)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White),
                        onClick = { if (isRunning) onCancelTest() else onStartTest() }
                    )
                    .testTag("start_test_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = primaryButtonText,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp
                )
            }

            // Secondary Share / Precision Button (Cyan Accent)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF00FFD1).copy(alpha = 0.12f))
                    .border(1.dp, Color(0xFF00FFD1).copy(alpha = 0.50f), RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color(0xFF00FFD1)),
                        onClick = {
                            if (testState.phase == TestPhase.COMPLETED) {
                                handleInitiateShare()
                            } else {
                                onStartPrecisionTest()
                            }
                        }
                    )
                    .testTag("precision_or_share_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (testState.phase == TestPhase.COMPLETED) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF00FFD1),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (language == Language.TH) "แชร์ข้อมูล" else "Share Report",
                            color = Color(0xFF00FFD1),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        )
                    } else {
                        Text(
                            text = if (language == Language.TH) "ดูผลละเอียด" else "Precision Mode",
                            color = Color(0xFF00FFD1),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // =========================================================================
        // 4. Symmetrical 2x2 Connection Metrics Grid with Sparklines
        // =========================================================================
        MetricsGrid(
            testState = testState,
            speedUnit = speedUnit,
            language = language,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // =========================================================================
        // 5. Symmetrical 2x2 Experience Assessment View (Browsing, Gaming, Video, Call)
        // =========================================================================
        ExperienceAssessmentView(
            testState = testState,
            language = language,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // =========================================================================
        // 6. AdMob Feed Banner View
        // =========================================================================
        AdMobBannerView(
            isVipAdFree = isVipAdFree,
            onRemoveAdsClick = onOpenVipModal,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // =========================================================================
        // 7. Clean Connection Info Card (Server, Ping, IP)
        // =========================================================================
        ConnectionInfoCard(
            server = selectedServer,
            ipInfo = ipInfo,
            language = language,
            onOpenServerModal = onOpenServerModal,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    // =========================================================================
    // 8. 5-Second AdMob Interstitial Countdown Modal Before Sharing
    // =========================================================================
    if (showShareAdCountdownModal) {
        ShareAdCountdownModal(
            language = language,
            onAdCompletedOrSkipped = {
                showShareAdCountdownModal = false
                showShareDetailModal = true
            },
            onOpenVipModal = {
                showShareAdCountdownModal = false
                onOpenVipModal()
            },
            onDismiss = { showShareAdCountdownModal = false }
        )
    }

    // =========================================================================
    // 9. Detailed Share Report Modal
    // =========================================================================
    if (showShareDetailModal) {
        ShareDetailModal(
            reportData = ShareReportData(
                downloadMbps = testState.downloadMbps ?: testState.liveSpeed.coerceAtLeast(10.0),
                uploadMbps = testState.uploadMbps ?: (testState.liveSpeed * 0.4).coerceAtLeast(5.0),
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
            onDismiss = { showShareDetailModal = false }
        )
    }
}
