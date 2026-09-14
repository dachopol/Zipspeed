package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.ServerInfo
import com.example.model.SpeedUnit
import com.example.ui.components.verticalScrollbar
import com.example.ui.theme.CyberInk
import com.example.ui.theme.CyberMuted
import com.example.ui.theme.CyberPanel
import com.example.ui.theme.CyberSubtle
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple

@Composable
fun SettingsScreen(
    language: Language,
    speedUnit: SpeedUnit,
    selectedServer: ServerInfo,
    autoSaveHistory: Boolean,
    reducedMotion: Boolean,
    batterySaver: Boolean,
    onLanguageChange: (Language) -> Unit,
    onSpeedUnitChange: (SpeedUnit) -> Unit,
    onOpenServerModal: () -> Unit,
    onToggleAutoSave: () -> Unit,
    onToggleReducedMotion: () -> Unit,
    onToggleBatterySaver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }
    var showFeedbackNotice by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .verticalScrollbar(scrollState)
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("settings_screen")
    ) {
        Text(
            text = stringResource(R.string.str_settings_support_95),
            color = CyberInk,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.testTag("settings_title")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Settings Section 1: General Preferences (การตั้งค่าทั่วไป)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CyberPanel)
                .border(1.dp, Color(0x17FFFFFF), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = NeonBlue,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.str_general_preferences_96),
                    color = CyberMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Language setting
            SettingsRow(
                title = stringResource(R.string.str_app_language_97),
                subtitle = stringResource(R.string.str_english_thai_98),
                trailing = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Language.entries.forEach { lang ->
                            val active = lang == language
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) NeonBlue else Color(0x1AFFFFFF))
                                    .clickable { onLanguageChange(lang) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = lang.displayName,
                                    color = if (active) Color.White else CyberMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Unit setting
            SettingsRow(
                title = stringResource(R.string.str_speed_unit_99),
                subtitle = stringResource(R.string.str_mbps_or_mb_s_100),
                trailing = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SpeedUnit.entries.forEach { unit ->
                            val active = unit == speedUnit
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) NeonGreen else Color(0x1AFFFFFF))
                                    .clickable { onSpeedUnitChange(unit) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = unit.label,
                                    color = if (active) Color(0xFF071510) else CyberMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Server Selection row
            SettingsRow(
                title = stringResource(R.string.str_test_server_101),
                subtitle = "${selectedServer.name} (${selectedServer.basePingMs}ms)",
                onClick = onOpenServerModal,
                trailing = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Select Server",
                        tint = CyberMuted
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Section 2: Advanced Options & System (ตัวเลือกเพิ่มเติม)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CyberPanel)
                .border(1.dp, Color(0x17FFFFFF), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.str_performance_system_102),
                    color = CyberMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Auto-Save Toggle
            SettingsRow(
                title = stringResource(R.string.str_auto_save_history_103),
                subtitle = stringResource(R.string.str_save_test_results_automaticall_104),
                trailing = {
                    Switch(
                        checked = autoSaveHistory,
                        onCheckedChange = { onToggleAutoSave() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeonBlue,
                            uncheckedThumbColor = CyberMuted,
                            uncheckedTrackColor = Color(0x1AFFFFFF)
                        ),
                        modifier = Modifier.testTag("auto_save_toggle")
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Reduced Motion Toggle
            SettingsRow(
                title = stringResource(R.string.str_reduced_motion_105),
                subtitle = stringResource(R.string.str_disable_gauge_rotation_visual__106),
                trailing = {
                    Switch(
                        checked = reducedMotion,
                        onCheckedChange = { onToggleReducedMotion() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeonPurple,
                            uncheckedThumbColor = CyberMuted,
                            uncheckedTrackColor = Color(0x1AFFFFFF)
                        ),
                        modifier = Modifier.testTag("reduced_motion_toggle")
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Battery Saver Mode Toggle
            SettingsRow(
                title = stringResource(R.string.str_battery_saver_mode_107),
                subtitle = stringResource(R.string.str_reduce_battery_drain_during_hi_108),
                trailing = {
                    Switch(
                        checked = batterySaver,
                        onCheckedChange = { onToggleBatterySaver() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeonGreen,
                            uncheckedThumbColor = CyberMuted,
                            uncheckedTrackColor = Color(0x1AFFFFFF)
                        ),
                        modifier = Modifier.testTag("battery_saver_toggle")
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Section 3: Support & FAQ (การสนับสนุน & ช่วยเหลือ)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CyberPanel)
                .border(1.dp, Color(0x17FFFFFF), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = NeonBlue,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.str_frequently_asked_questions_109),
                    color = CyberMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val faqs = if (language == Language.TH) {
                listOf(
                    "การทดสอบวัดจากเซิร์ฟเวอร์จริงหรือไม่?" to
                            "ใช่แล้ว แอปพลิเคชันเชื่อมต่อไปยัง Cloudflare CDN Edge nodes จริง โดยวัดความเร็วการรับส่งข้อมูลผ่าน HTTP Streaming จริง 100%",
                    "Ping, Jitter และ Packet Loss ต่างกันอย่างไร?" to
                            "Ping คือเวลาในการเดินทางของข้อมูลไปกลับ (ms), Jitter คือความผันผวนของค่า Ping ยิ่งน้อยยิ่งเสถียร, ส่วน Packet Loss คือข้อมูลที่สูญหายระหว่างทาง",
                    "ทำไมความเร็วบน Wi-Fi จึงต่างจาก 4G/5G?" to
                            "ความเร็วขึ้นอยู่กับความถี่คลื่น (2.4GHz / 5GHz / 6GHz), ระยะห่างจากเราเตอร์, สัญญาณรบกวนในบริเวณใกล้เคียง และความหนาแน่นของผู้ใช้งานในพื้นที่"
                )
            } else {
                listOf(
                    "Are the tests conducted on real servers?" to
                            "Yes, the application directly connects to real Cloudflare CDN Edge nodes and measures throughput using actual HTTP streaming chunks.",
                    "What are Ping, Jitter, and Packet Loss?" to
                            "Ping is round-trip network latency in milliseconds. Jitter is variance in latency (lower is better for gaming/calls). Packet loss is percentage of packets lost in transit.",
                    "Why is Wi-Fi speed different from 4G/5G cellular?" to
                            "Speed depends on frequency band (2.4GHz vs 5GHz), router distance, channel interference, and cellular base station congestion."
                )
            }

            faqs.forEachIndexed { index, (q, a) ->
                val isExpanded = expandedFaqIndex == index
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x0EFFFFFF))
                        .clickable { expandedFaqIndex = if (isExpanded) null else index }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QuestionAnswer,
                                contentDescription = null,
                                tint = NeonBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = q,
                                color = CyberInk,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = CyberMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = a,
                                color = CyberSubtle,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                if (index < faqs.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Feedback Contact
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x10FFFFFF))
                    .clickable { showFeedbackNotice = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = NeonBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.str_contact_support_feedback_110),
                            color = NeonBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "support@zipspeed.app",
                            color = CyberMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                Text(
                    text = if (showFeedbackNotice) "ส่งแล้ว" else "ติดต่อ",
                    color = NeonGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Version Info Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CyberPanel)
                .border(1.dp, Color(0x17FFFFFF), RoundedCornerShape(16.dp))
                .padding(vertical = 14.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Zipspeed Network Speedtest",
                color = CyberInk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.str_version_v_com_example_buildcon_111, com.example.BuildConfig.VERSION_NAME, com.example.BuildConfig.VERSION_CODE),
                color = NeonGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.testTag("settings_version_info")
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = CyberInk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = CyberMuted,
                fontSize = 12.sp
            )
        }
        trailing()
    }
}
