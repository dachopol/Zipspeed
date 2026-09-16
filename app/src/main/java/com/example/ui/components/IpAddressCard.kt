package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.NetworkIpInfo
import com.example.model.Language
import com.example.ui.theme.CyberInk
import com.example.ui.theme.CyberMuted
import com.example.ui.theme.CyberPanel
import com.example.ui.theme.NeonGreen

@Composable
fun IpAddressCard(
    ipInfo: NetworkIpInfo,
    language: Language,
    onRefreshIp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "ipSpin")
    val spinAngle by if (ipInfo.isFetching) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing)
            ),
            label = "spin"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CyberPanel)
            .border(1.dp, Color(0x17FFFFFF), RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("ip_address_card")
    ) {
        // Card Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x2020D9A0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "IP Address",
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.str_ip_address_4),
                    color = CyberMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            }

            IconButton(
                onClick = onRefreshIp,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("refresh_ip_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh IP",
                    tint = CyberMuted,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(spinAngle)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Public IP Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.str_public_ip_5),
                    color = CyberMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = ipInfo.publicIp ?: "--",
                    color = CyberInk,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.testTag("public_ip_text")
                )
            }

            // Copy Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x1AFFFFFF))
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("IP Address", ipInfo.publicIp)
                        clipboard.setPrimaryClip(clip)
                        val msg = context.getString(R.string.str_ip_copied_ipinfo_publicip_6, ipInfo.publicIp)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("copy_ip_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy IP",
                    tint = Color(0xFFA9C6FF),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = stringResource(R.string.str_copy_7),
                    color = Color(0xFFA9C6FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Local IP & ISP Sub-row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x0CFFFFFF))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Router,
                    contentDescription = null,
                    tint = CyberMuted,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${stringResource(R.string.str_local_ip_8)}: ${ipInfo.localIp}",
                    color = CyberMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = ipInfo.ispName ?: "Unknown Network",
                color = NeonGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
