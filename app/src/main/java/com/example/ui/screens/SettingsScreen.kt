package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Language
import com.example.model.ServerInfo
import com.example.model.SpeedUnit
import com.example.ui.components.verticalScrollbar
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.WinePink

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
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    isSecurityShieldActive: Boolean = true,
    onToggleDarkLight: () -> Unit = {},
    onToggleSecurityShield: (Boolean) -> Unit = {},
    onOpenSecurityModal: () -> Unit = {}
) {
    val theme = LocalAppTheme.current
    val scrollState = rememberScrollState()
    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    val isTh = language == Language.TH

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .verticalScrollbar(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("settings_screen")
    ) {
        Text(
            text = if (isTh) "การตั้งค่าและความช่วยเหลือ" else "Settings & Support",
            color = theme.colors.textMain,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold, // 600
            modifier = Modifier.testTag("settings_title")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // =========================================================================
        // Section 1: Display & Preferences (ธีม, ภาษา, หน่วยความเร็ว)
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(theme.colors.cardBg)
                .border(1.dp, theme.colors.border, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = ChampagneGold,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isTh) "การแสดงผล & ภาษา" else "Display & Language",
                    color = theme.colors.textMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dark / Light Theme Toggle (Moved from Top Header)
            SettingsRow(
                title = if (isTh) "โหมดธีม" else "Theme Appearance",
                subtitle = if (isDarkTheme) (if (isTh) "โหมดมืด (Navy Dark)" else "Deep Navy Dark") else (if (isTh) "โหมดสว่าง (Ivory Light)" else "Ivory Light"),
                trailing = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (theme.isDark) Color(0x1AFFFFFF) else Color(0xFFF1F5F9))
                            .border(1.dp, theme.colors.border, RoundedCornerShape(10.dp))
                            .clickable { onToggleDarkLight() }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("theme_toggle_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = ChampagneGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isDarkTheme) (if (isTh) "เปลี่ยนเป็นสว่าง" else "Light") else (if (isTh) "เปลี่ยนเป็นมืด" else "Dark"),
                                color = theme.colors.textMain,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Language setting (Moved from Top Header)
            SettingsRow(
                title = if (isTh) "ภาษาของแอป" else "App Language",
                subtitle = if (isTh) "ไทย / English" else "English / Thai",
                trailing = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Language.entries.forEach { lang ->
                            val active = lang == language
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) ChampagneGold else Color(0x12FFFFFF))
                                    .clickable { onLanguageChange(lang) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = lang.displayName,
                                    color = if (active) Color(0xFF0B1120) else theme.colors.textMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Speed Unit setting
            SettingsRow(
                title = if (isTh) "หน่วยความเร็ว" else "Speed Unit",
                subtitle = if (isTh) "เมกะบิต/วินาที หรือ เมกะไบต์/วินาที" else "Mbps or MB/s",
                trailing = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SpeedUnit.entries.forEach { unit ->
                            val active = unit == speedUnit
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) ChampagneGold else Color(0x12FFFFFF))
                                    .clickable { onSpeedUnitChange(unit) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = unit.label,
                                    color = if (active) Color(0xFF0B1120) else theme.colors.textMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =========================================================================
        // Section 2: Security & Privacy (ย้าย Security จากแถบล่างมาไว้ที่นี่)
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(theme.colors.cardBg)
                .border(1.dp, theme.colors.border, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = if (isSecurityShieldActive) StatusGreen else theme.colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isTh) "ความปลอดภัย & ความเป็นส่วนตัว" else "Security & Privacy",
                    color = theme.colors.textMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security Shield Switch
            SettingsRow(
                title = if (isTh) "ระบบตรวจสอบความปลอดภัย (Security Shield)" else "Security Shield Protection",
                subtitle = if (isSecurityShieldActive) {
                    if (isTh) "เปิดใช้งาน • ตรวจสอบ DNS Leak และเปิดพอร์ต" else "Active • DNS Leak & Port audit"
                } else {
                    if (isTh) "ปิดอยู่ • แตะเพื่อเปิดการตรวจสอบ" else "Inactive • Tap to enable"
                },
                trailing = {
                    Switch(
                        checked = isSecurityShieldActive,
                        onCheckedChange = { onToggleSecurityShield(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StatusGreen,
                            uncheckedThumbColor = theme.colors.textMuted,
                            uncheckedTrackColor = Color(0x1AFFFFFF)
                        ),
                        modifier = Modifier.testTag("security_shield_toggle")
                    )
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Security Audit Report Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x0EFFFFFF))
                    .clickable { onOpenSecurityModal() }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isTh) "ดูรายงานการประเมินความปลอดภัย" else "View Security Audit Report",
                    color = ChampagneGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ChampagneGold,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =========================================================================
        // Section 3: Performance & Network
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(theme.colors.cardBg)
                .border(1.dp, theme.colors.border, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = ChampagneGold,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isTh) "ประสิทธิภาพ & เซิร์ฟเวอร์" else "Performance & Server",
                    color = theme.colors.textMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Server Selection row
            SettingsRow(
                title = if (isTh) "เซิร์ฟเวอร์ทดสอบ" else "Test Server",
                subtitle = "${selectedServer.name} (${selectedServer.basePingMs}ms)",
                onClick = onOpenServerModal,
                trailing = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Select Server",
                        tint = theme.colors.textMuted
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-Save Toggle
            SettingsRow(
                title = if (isTh) "บันทึกผลอัตโนมัติ" else "Auto-Save History",
                subtitle = if (isTh) "บันทึกประวัติทุกครั้งที่ทดสอบเสร็จ" else "Save test results automatically",
                trailing = {
                    Switch(
                        checked = autoSaveHistory,
                        onCheckedChange = { onToggleAutoSave() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ChampagneGold,
                            uncheckedThumbColor = theme.colors.textMuted,
                            uncheckedTrackColor = Color(0x1AFFFFFF)
                        ),
                        modifier = Modifier.testTag("auto_save_toggle")
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Reduced Motion Toggle
            SettingsRow(
                title = if (isTh) "ลดการเคลื่อนไหว (Reduced Motion)" else "Reduced Motion",
                subtitle = if (isTh) "ลดอนิเมชันเพื่อประหยัดแบตเตอรี่และสายตา" else "Simplify animations",
                trailing = {
                    Switch(
                        checked = reducedMotion,
                        onCheckedChange = { onToggleReducedMotion() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ChampagneGold,
                            uncheckedThumbColor = theme.colors.textMuted,
                            uncheckedTrackColor = Color(0x1AFFFFFF)
                        ),
                        modifier = Modifier.testTag("reduced_motion_toggle")
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Battery Saver Mode Toggle
            SettingsRow(
                title = if (isTh) "โหมดประหยัดพลังงาน" else "Battery Saver Mode",
                subtitle = if (isTh) "ลดความถี่การอัปเดตกราฟระหว่างทดสอบ" else "Lower visual refresh rate during test",
                trailing = {
                    Switch(
                        checked = batterySaver,
                        onCheckedChange = { onToggleBatterySaver() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ChampagneGold,
                            uncheckedThumbColor = theme.colors.textMuted,
                            uncheckedTrackColor = Color(0x1AFFFFFF)
                        ),
                        modifier = Modifier.testTag("battery_saver_toggle")
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =========================================================================
        // Section 4: Support & FAQ
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(theme.colors.cardBg)
                .border(1.dp, theme.colors.border, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = ChampagneGold,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isTh) "คำถามที่พบบ่อย & ช่วยเหลือ" else "Frequently Asked Questions",
                    color = theme.colors.textMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val faqs = if (isTh) {
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
                                tint = ChampagneGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = q,
                                color = theme.colors.textMain,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = theme.colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = a,
                                color = theme.colors.textMuted,
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
                    .background(Color(0x0EFFFFFF))
                    .clickable {
                        val emailIntent = Intent(
                            Intent.ACTION_SENDTO,
                            Uri.parse("mailto:215334638+AnakinYoo@users.noreply.github.com")
                        ).apply {
                            putExtra(Intent.EXTRA_SUBJECT, "Zipspeed Support")
                        }
                        runCatching { context.startActivity(emailIntent) }
                    }
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
                        tint = ChampagneGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = if (isTh) "ติดต่อผู้พัฒนา / แจ้งปัญหา" else "Contact Support / Feedback",
                            color = ChampagneGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isTh) "เขียนอีเมลถึง AnakinYoo" else "Write an email to AnakinYoo",
                            color = theme.colors.textMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                Text(
                    text = if (isTh) "เขียนจดหมาย" else "Email",
                    color = StatusGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =========================================================================
        // Section 5: App Version & Attribution
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(theme.colors.cardBg)
                .border(1.dp, theme.colors.border, RoundedCornerShape(16.dp))
                .padding(vertical = 14.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Zipspeed by AnakinYoo",
                color = theme.colors.textMain,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Version ${com.example.BuildConfig.VERSION_NAME} (Build ${com.example.BuildConfig.VERSION_CODE})",
                color = ChampagneGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.testTag("settings_version_info")
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    val theme = LocalAppTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = title,
                color = theme.colors.textMain,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = theme.colors.textMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        trailing()
    }
}
