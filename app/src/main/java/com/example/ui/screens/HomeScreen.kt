package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SpeedTestRecord
import com.example.engine.NetworkIpInfo
import com.example.model.Language
import com.example.model.ServerInfo
import com.example.model.SpeedTestState
import com.example.model.SpeedUnit
import com.example.model.TestPhase
import com.example.model.VideoTestState
import com.example.model.WebTestState
import com.example.ui.components.AdMobBannerView
import com.example.ui.components.ExperienceAssessmentView
import com.example.ui.components.MetricsGrid
import com.example.ui.components.ShareAdCountdownModal
import com.example.ui.components.ShareDetailModal
import com.example.ui.components.ShareReportData
import com.example.ui.components.SpeedGauge
import com.example.ui.components.verticalScrollbar
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WinePink
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    isGpsActive: Boolean = false,
    previousResult: SpeedTestRecord? = null,
    videoState: VideoTestState = VideoTestState(),
    webState: WebTestState = WebTestState(),
    onStartTest: () -> Unit,
    onStartPrecisionTest: () -> Unit,
    onCancelTest: () -> Unit,
    onTogglePrecisionMode: () -> Unit = {},
    onStartVideoTest: () -> Unit = {},
    onCancelVideoTest: () -> Unit = {},
    onStartWebTest: () -> Unit = {},
    onCancelWebTest: () -> Unit = {},
    onOpenServerModal: () -> Unit,
    onSelectServer: (ServerInfo) -> Unit = {},
    onRefreshIp: () -> Unit,
    onToggleSpeedUnit: () -> Unit = {},
    onToggleGpsMode: () -> Unit = {},
    onOpenVipModal: () -> Unit = {},
    onNavigateToResults: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isRunning = testState.phase == TestPhase.TESTING_PING ||
            testState.phase == TestPhase.TESTING_DOWNLOAD ||
            testState.phase == TestPhase.TESTING_UPLOAD

    var showShareAdCountdownModal by remember { mutableStateOf(false) }
    var showShareDetailModal by remember { mutableStateOf(false) }
    var isNetworkDetailsExpanded by remember { mutableStateOf(false) }

    val theme = LocalAppTheme.current
    val isDark = theme.isDark
    val isTh = language == Language.TH

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
            showShareDetailModal = true
        } else {
            showShareAdCountdownModal = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .verticalScrollbar(scrollState)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // =========================================================================
            // 1. Compact Network & Server Card (Decluttered & Integrated)
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.colors.cardBg)
                    .border(1.dp, theme.colors.border, RoundedCornerShape(20.dp))
                    .padding(14.dp)
                    .testTag("network_info_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Row: ISP Name + Status Green Dot & Selected Server + Change Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ISP and Connected status
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusGreen)
                            )
                            Column {
                                Text(
                                    text = if (!ipInfo.ispName.isNullOrBlank()) ipInfo.ispName!! else "Edge CDN Network",
                                    color = theme.colors.textMain,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isTh) "เชื่อมต่อแล้ว" else "Connected",
                                    color = StatusGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        // Server Selector Button
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0x12FFFFFF) else Color(0xFFF1F5F9))
                                .border(1.dp, theme.colors.border, RoundedCornerShape(12.dp))
                                .clickable { onOpenServerModal() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("change_server_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = selectedServer.name,
                                    color = theme.colors.textMain,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isTh) "เปลี่ยน" else "Change",
                                    color = ZipMint,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Expandable Toggle for Network Details
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isNetworkDetailsExpanded = !isNetworkDetailsExpanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTh) "รายละเอียดเครือข่าย" else "Network Details",
                            color = theme.colors.textMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Icon(
                            imageVector = if (isNetworkDetailsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isNetworkDetailsExpanded) "Collapse" else "Expand",
                            tint = theme.colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Collapsible Network Details Content
                    AnimatedVisibility(
                        visible = isNetworkDetailsExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Public IP & Local IP
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Public IP: ${ipInfo.publicIp ?: "--"}",
                                    color = theme.colors.textMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (!ipInfo.localIp.isNullOrBlank()) {
                                    Text(
                                        text = "Local: ${ipInfo.localIp}",
                                        color = theme.colors.textMuted,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Anycast PoP Node & Location
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Edge Node: ${ipInfo.colo ?: "Anycast"} • ${selectedServer.subLocation}",
                                    color = theme.colors.textMuted,
                                    fontSize = 11.sp
                                )

                                // GPS Toggle & IP Refresh
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isGpsActive) StatusGreen.copy(alpha = 0.15f) else Color(0x10FFFFFF))
                                            .clickable {
                                                locationPermissionLauncher.launch(
                                                    arrayOf(
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                                    )
                                                )
                                            }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (isGpsActive) "GPS: ON" else "GPS",
                                            color = if (isGpsActive) StatusGreen else theme.colors.textMuted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x10FFFFFF))
                                            .clickable { onRefreshIp() }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Refresh IP",
                                            tint = theme.colors.textMuted,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================================
            // 2. 3D Speed Gauge Hero
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
                onStartTest = {
                    if (isPrecisionMode) onStartPrecisionTest() else onStartTest()
                },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Test Phase Status Banner
            if (isRunning) {
                Spacer(modifier = Modifier.height(6.dp))
                val phaseMsg = when (testState.phase) {
                    TestPhase.TESTING_PING -> if (isTh) "กำลังวัด Latency (HTTP RTT) ไปยังเซิร์ฟเวอร์..." else "Measuring HTTP Latency to Edge Node..."
                    TestPhase.TESTING_DOWNLOAD -> if (isTh) "กำลังวัดความเร็วดาวน์โหลด (${testState.downloadSamples.size} ตัวอย่าง)..." else "Testing real download throughput..."
                    TestPhase.TESTING_UPLOAD -> if (isTh) "กำลังวัดความเร็วอัปโหลด (${testState.uploadSamples.size} ตัวอย่าง)..." else "Testing real upload throughput..."
                    else -> ""
                }
                Text(
                    text = phaseMsg,
                    color = ZipMint,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            } else if (testState.phase == TestPhase.ERROR) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = WinePink,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = testState.errorMessage ?: if (isTh) "ไม่สามารถเชื่อมต่อได้ กรุณาลองใหม่อีกครั้ง" else "Connection failed, please retry",
                        color = WinePink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================================
            // 3. Running-state control only. Idle/restart actions live on the gauge to avoid duplicates.
            // =========================================================================
            if (isRunning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(WinePink.copy(alpha = 0.14f))
                        .border(1.dp, WinePink.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = WinePink),
                            onClick = onCancelTest
                        )
                        .testTag("cancel_test_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = WinePink,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isTh) "หยุดการทดสอบ" else "Stop Test",
                            color = WinePink,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // =========================================================================
            // 4. Precision Mode Small Switch with Explanation Directly Below Button
            // =========================================================================
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = if (isTh) "โหมดความแม่นยำสูง (Precision Mode)" else "Precision Mode",
                        color = theme.colors.textMain,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isTh) {
                            "เพิ่มขนาดข้อมูลและรอบทดสอบเพื่อวัด Throughput สูงสุด"
                        } else {
                            "Transfers larger payloads to verify peak capacity"
                        },
                        color = theme.colors.textMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                Switch(
                    checked = isPrecisionMode,
                    onCheckedChange = { if (!isRunning) onTogglePrecisionMode() },
                    enabled = !isRunning,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ZipMint,
                        uncheckedThumbColor = theme.colors.textMuted,
                        uncheckedTrackColor = Color(0x1AFFFFFF)
                    ),
                    modifier = Modifier.testTag("precision_mode_switch")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // 5. Prominent Metrics Grid (Download & Upload prominent, Ping & Jitter next)
            // =========================================================================
            MetricsGrid(
                testState = testState,
                speedUnit = speedUnit,
                language = language,
                modifier = Modifier.fillMaxWidth()
            )

            // =========================================================================
            // 6. Share Result Action (If completed)
            // =========================================================================
            if (testState.phase == TestPhase.COMPLETED) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ZipMint.copy(alpha = 0.12f))
                        .border(1.dp, ZipMint.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .clickable { handleInitiateShare() }
                        .testTag("share_result_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = ZipMint,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isTh) "แชร์รายงานผลการทดสอบ" else "Share Speed Test Report",
                            color = ZipMint,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // =========================================================================
            // 7. Previous Result Card (Distinguished clearly when IDLE / CANCELLED)
            // =========================================================================
            if ((testState.phase == TestPhase.IDLE || testState.phase == TestPhase.CANCELLED) && previousResult != null) {
                Spacer(modifier = Modifier.height(16.dp))
                PreviousResultCard(
                    record = previousResult,
                    speedUnit = speedUnit,
                    language = language,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================================
            // 8. Experience Assessment View
            // =========================================================================
            ExperienceAssessmentView(
                testState = testState,
                language = language,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================================
            // 9. AdMob Feed Banner View
            // =========================================================================
            AdMobBannerView(
                isVipAdFree = isVipAdFree,
                onRemoveAdsClick = onOpenVipModal,
                modifier = Modifier.fillMaxWidth()
            )

            // Safe bottom margin for BottomNavBar
            Spacer(modifier = Modifier.height(84.dp))
        }
    }

    // Modal Overlays
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

    if (showShareDetailModal) {
        ShareDetailModal(
            reportData = ShareReportData(
                downloadMbps = testState.downloadMbps ?: 0.0,
                uploadMbps = testState.uploadMbps ?: 0.0,
                pingMs = testState.pingMs ?: selectedServer.basePingMs,
                jitterMs = testState.jitterMs ?: 2,
                packetLossPercent = 0.0,
                serverName = selectedServer.name,
                networkType = if (isPrecisionMode) "Precision Anycast" else "Standard Anycast",
                publicIp = ipInfo.publicIp ?: "--",
                ispName = ipInfo.ispName ?: "--"
            ),
            speedUnit = speedUnit,
            language = language,
            onDismiss = { showShareDetailModal = false }
        )
    }
}

/**
 * Previous Test Result Card
 */
@Composable
private fun PreviousResultCard(
    record: SpeedTestRecord,
    speedUnit: SpeedUnit,
    language: Language,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val isTh = language == Language.TH

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(record.timestamp) { dateFormat.format(Date(record.timestamp)) }

    val dlDisplay = if (speedUnit == SpeedUnit.MB_S) record.downloadMbps / 8.0 else record.downloadMbps
    val ulDisplay = if (speedUnit == SpeedUnit.MB_S) record.uploadMbps / 8.0 else record.uploadMbps

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(theme.colors.cardBg)
            .border(1.dp, theme.colors.border, RoundedCornerShape(20.dp))
            .padding(14.dp)
            .testTag("previous_result_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = ChampagneGold,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (isTh) "ผลการทดสอบล่าสุด (ครั้งก่อนหน้า)" else "Previous Test Result",
                        color = theme.colors.textMain,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = formattedDate,
                    color = theme.colors.textMuted,
                    fontSize = 11.sp
                )
            }

            Text(
                text = if (isTh) {
                    "บันทึกครั้งก่อน • เซิร์ฟเวอร์: ${record.serverName}"
                } else {
                    "Last run • Server: ${record.serverName}"
                },
                color = theme.colors.textMuted.copy(alpha = 0.75f),
                fontSize = 10.5.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            // 4 Metrics Mini Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = ChampagneGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isTh) "ดาวน์โหลด" else "Down",
                            color = theme.colors.textMuted,
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%.1f %s", dlDisplay, speedUnit.label),
                        color = theme.colors.textMain,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = WinePink,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isTh) "อัปโหลด" else "Up",
                            color = theme.colors.textMuted,
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%.1f %s", ulDisplay, speedUnit.label),
                        color = theme.colors.textMain,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = ChampagneGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Latency",
                            color = theme.colors.textMuted,
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = "${record.pingMs} ms",
                        color = theme.colors.textMain,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = theme.colors.textMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Jitter",
                            color = theme.colors.textMuted,
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = "${record.jitterMs} ms",
                        color = theme.colors.textMain,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
