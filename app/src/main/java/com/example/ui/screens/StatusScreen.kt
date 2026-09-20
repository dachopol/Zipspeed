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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.engine.EndpointHealthResult
import com.example.engine.NetworkIpInfo
import com.example.model.Language
import com.example.ui.components.DowndetectorWidget
import com.example.ui.components.verticalScrollbar
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.WinePink
import com.example.util.startActivitySafely

@Composable
fun StatusScreen(
    ipInfo: NetworkIpInfo,
    endpointHealth: List<EndpointHealthResult>,
    isEndpointChecking: Boolean,
    language: Language,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val isTh = language == Language.TH
    val context = LocalContext.current
    val scrollState = rememberScrollState()

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
                .widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                                text = if (isTh) "สถานะเครือข่าย" else "Network Status",
                                color = theme.colors.textMain,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isTh)
                                    "IP, ISP, Anycast และ HTTP endpoint ที่ตรวจได้จริง"
                                else
                                    "Observed IP, ISP, Anycast and measured HTTP endpoints",
                                color = theme.colors.textMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefresh,
                        enabled = !isEndpointChecking && !ipInfo.isFetching,
                        modifier = Modifier.testTag("status_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = if (isTh) "รีเฟรชสถานะ" else "Refresh status",
                            tint = ChampagneGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isTh) "ข้อมูลที่ตรวจพบ" else "Observed connection data",
                        color = theme.colors.textMain,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    StatusValueRow("Public IP", ipInfo.publicIp ?: "--")
                    StatusValueRow("Local IPv4", ipInfo.localIp ?: "--")
                    StatusValueRow("ISP / ASN", ipInfo.ispName ?: "--", highlight = true)
                    StatusValueRow("Anycast PoP", ipInfo.colo ?: "--")
                    StatusValueRow(
                        if (isTh) "ประเทศ" else "Country",
                        ipInfo.countryCode ?: "--"
                    )
                    if (!ipInfo.locationStatusText.isBlank()) {
                        Text(
                            text = ipInfo.locationStatusText,
                            color = theme.colors.textMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                            text = if (isTh) "HTTP Endpoint Health" else "HTTP Endpoint Health",
                            color = theme.colors.textMain,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isEndpointChecking) {
                                if (isTh) "กำลังตรวจ..." else "Checking..."
                            } else {
                                if (endpointHealth.isEmpty()) "--" else if (isTh) "ผลล่าสุด" else "Latest"
                            },
                            color = theme.colors.textMuted,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (endpointHealth.isEmpty() && !isEndpointChecking) {
                        Text(
                            text = if (isTh)
                                "ยังไม่มีผล กดรีเฟรชเพื่อทดสอบ endpoint"
                            else
                                "No endpoint results yet. Tap refresh to run checks.",
                            color = theme.colors.textMuted,
                            fontSize = 11.sp
                        )
                    }

                    endpointHealth.forEach { ep ->
                        EndpointRow(
                            ep = ep,
                            isTh = isTh,
                            onOpen = {
                                context.startActivitySafely(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(ep.url))
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            DowndetectorWidget(
                language = language,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}

@Composable
private fun StatusValueRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    val theme = LocalAppTheme.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = theme.colors.textMuted, fontSize = 12.sp)
        Text(
            text = value,
            color = if (highlight) ChampagneGold else theme.colors.textMain,
            fontSize = 12.sp,
            fontFamily = if (highlight) FontFamily.Default else FontFamily.Monospace,
            fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun EndpointRow(
    ep: EndpointHealthResult,
    isTh: Boolean,
    onOpen: () -> Unit
) {
    val theme = LocalAppTheme.current
    val statusColor = if (ep.isReachable) StatusGreen else WinePink

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(vertical = 9.dp),
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
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = if (ep.isReachable && ep.latencyMs != null) {
                    "${ep.latencyMs} ms • HTTP ${ep.statusCode ?: "--"}"
                } else {
                    if (isTh) "ไม่พร้อมใช้งาน" else "Unavailable"
                },
                color = statusColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = if (isTh) "เปิดเว็บไซต์" else "Open website",
                tint = theme.colors.textMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
