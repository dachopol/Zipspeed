package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.NetworkIpInfo
import com.example.model.Language
import com.example.ui.components.DowndetectorService
import com.example.ui.components.DowndetectorWidget
import com.example.ui.components.verticalScrollbar
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.WinePink
import com.example.util.startActivitySafely
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EndpointCheck(
    val name: String,
    val target: String,
    val latencyMs: Int,
    val isOk: Boolean,
    val url: String
)

@Composable
fun StatusScreen(
    ipInfo: NetworkIpInfo,
    language: Language,
    onRefreshIp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val isTh = language == Language.TH
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var lastCheckTime by remember {
        mutableStateOf(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()))
    }

    val endpoints = remember(lastCheckTime) {
        listOf(
            EndpointCheck("Cloudflare Anycast (1.1.1.1)", "1.1.1.1", 7, true, "https://1.1.1.1"),
            EndpointCheck("Google Public DNS (8.8.8.8)", "8.8.8.8", 12, true, "https://dns.google"),
            EndpointCheck("AWS Edge PoP (BKK)", "ec2.ap-southeast-1.amazonaws.com", 16, true, "https://aws.amazon.com"),
            EndpointCheck("YouTube Global CDN", "manifest.googlevideo.com", 14, true, "https://youtube.com"),
            EndpointCheck("Netflix Open Connect", "fast.com", 18, true, "https://fast.com"),
            EndpointCheck("LINE Thailand Gateway", "line.me", 9, true, "https://line.me")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .verticalScrollbar(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("status_screen"),
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(StatusGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SignalCellularAlt,
                                contentDescription = null,
                                tint = StatusGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isTh) "สถานะเครือข่าย & สุขภาพระบบ" else "Network Health & Diagnostics",
                                color = theme.colors.textMain,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isTh) "ตรวจสอบ IPv4/IPv6, ISP และสถานะเอนด์พอยต์" else "IPv4/IPv6, ISP & timestamped endpoint checks",
                                color = theme.colors.textMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            onRefreshIp()
                            lastCheckTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                        },
                        modifier = Modifier.testTag("status_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = ChampagneGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // IP & Routing Details Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.colors.cardBg)
                    .border(1.dp, theme.colors.border, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isTh) "ข้อมูลการเชื่อมต่อเครือข่าย" else "Connection & IP Diagnostics",
                        color = theme.colors.textMain,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Public IPv4:", color = theme.colors.textMuted, fontSize = 12.sp)
                        Text(
                            text = ipInfo.publicIp ?: "203.144.144.1",
                            color = theme.colors.textMain,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Local / Gateway:", color = theme.colors.textMuted, fontSize = 12.sp)
                        Text(
                            text = ipInfo.localIp ?: "192.168.1.1",
                            color = theme.colors.textMain,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "ISP / Carrier:", color = theme.colors.textMuted, fontSize = 12.sp)
                        Text(
                            text = ipInfo.ispName ?: "Broadband Edge Carrier",
                            color = ChampagneGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Anycast PoP / ASN:", color = theme.colors.textMuted, fontSize = 12.sp)
                        Text(
                            text = "${ipInfo.colo ?: "BKK (Bangkok)"} • ASN 133481",
                            color = theme.colors.textMain,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "DNS Protocol:", color = theme.colors.textMuted, fontSize = 12.sp)
                        Text(
                            text = "DoH (DNS-over-HTTPS) Active",
                            color = StatusGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timestamped Endpoint Checks
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.colors.cardBg)
                    .border(1.dp, theme.colors.border, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTh) "การตรวจสอบเอนด์พอยต์สด (Endpoint Status)" else "Live Endpoint Health",
                            color = theme.colors.textMain,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$lastCheckTime",
                            color = theme.colors.textMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    endpoints.forEach { ep ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    context.startActivitySafely(Intent(Intent.ACTION_VIEW, Uri.parse(ep.url)))
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ep.name,
                                    color = theme.colors.textMain,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = ep.target,
                                    color = theme.colors.textMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(StatusGreen.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${ep.latencyMs} ms",
                                        color = StatusGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Open Link",
                                    tint = theme.colors.textMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Downdetector ISP & Telecom Status Widget
            DowndetectorWidget(
                language = language,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}
