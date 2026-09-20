package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.SpeedUnit
import com.example.util.startActivitySafely
import com.example.ui.theme.CyberInk
import com.example.ui.theme.CyberMuted
import com.example.ui.theme.CyberPanel
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ShareReportData(
    val downloadMbps: Double?,
    val uploadMbps: Double?,
    val pingMs: Int?,
    val jitterMs: Int? = null,
    val packetLossPercent: Double? = null,
    val serverName: String,
    val networkType: String? = null,
    val publicIp: String? = null,
    val ispName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareDetailModal(
    reportData: ShareReportData,
    speedUnit: SpeedUnit,
    language: Language,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(reportData.timestamp))

    val dlVal = reportData.downloadMbps?.let { if (speedUnit == SpeedUnit.MB_S) it / 8.0 else it }
    val ulVal = reportData.uploadMbps?.let { if (speedUnit == SpeedUnit.MB_S) it / 8.0 else it }
    val dlStr = dlVal?.let { String.format(Locale.US, "%.1f", it) } ?: "--"
    val ulStr = ulVal?.let { String.format(Locale.US, "%.1f", it) } ?: "--"

    val networkRating = when (val measured = reportData.downloadMbps) {
        null -> if (language == Language.TH) "ไม่มีข้อมูล" else "No data"
        else -> when {
            measured >= 500 -> stringResource(R.string.str_ultra_fast_5g_fiber_29)
            measured >= 100 -> stringResource(R.string.str_high_speed_performance_30)
            else -> stringResource(R.string.str_standard_broadband_31)
        }
    }

    val shareTextSummary = remember(reportData, speedUnit, language) {
        buildString {
            append("Zipspeed Network Test Report\n")
            append("----------------------------------\n")
            append("Download: $dlStr ${speedUnit.label}\n")
            append("Upload: $ulStr ${speedUnit.label}\n")
            append("Ping: ${reportData.pingMs?.let { "$it ms" } ?: "--"}")
            if (reportData.jitterMs != null) append(" | Jitter: ${reportData.jitterMs} ms")
            append("\n")
            append("Server: ${reportData.serverName}\n")
            if (!reportData.ispName.isNullOrBlank()) {
                append("Network: ${reportData.ispName}\n")
            } else if (!reportData.networkType.isNullOrBlank()) {
                append("Network: ${reportData.networkType}\n")
            }
            if (!reportData.publicIp.isNullOrBlank()) {
                append("Public IP: ${reportData.publicIp}\n")
            }
            append("Date: $formattedDate\n")
            append("----------------------------------\n")
            append("Measured by Zipspeed v${com.example.BuildConfig.VERSION_NAME}\n")
        }
    }

    fun doShareIntent(text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "Zipspeed Test Result")
            type = "text/plain"
        }
        val chooser = Intent.createChooser(
            sendIntent,
            context.getString(R.string.str_share_speed_test_details_32)
        )
        context.startActivitySafely(chooser)
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Zipspeed Report", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(
            context,
            context.getString(R.string.str_detailed_report_copied_to_clip_33),
            Toast.LENGTH_SHORT
        ).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F111A),
        scrimColor = Color(0x99000000)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("share_detail_modal")
        ) {
            // Header
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x222F7BFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Icon",
                            tint = NeonBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.str_detailed_speed_test_report_34),
                            color = CyberInk,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.testTag("share_modal_title")
                        )
                        Text(
                            text = formattedDate,
                            color = CyberMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = CyberMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Digital Report Card Preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF161C2E), Color(0xFF101424))
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(NeonBlue.copy(alpha = 0.5f), NeonPurple.copy(alpha = 0.4f))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
                    .testTag("share_card_preview")
            ) {
                // Network Quality Rating Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ZIPSPEED DIAGNOSTIC",
                        color = NeonBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x2210B981))
                            .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = networkRating,
                            color = NeonGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Download & Upload Big Numbers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Download
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.str_download_35),
                                color = CyberMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dlStr,
                            color = CyberInk,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = speedUnit.label,
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 1.dp, height = 40.dp)
                            .background(Color(0x20FFFFFF))
                    )

                    // Upload
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.str_upload_36),
                                color = CyberMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ulStr,
                            color = CyberInk,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = speedUnit.label,
                            color = NeonPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x15FFFFFF))
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Additional Metrics Row (Ping, Jitter, Packet Loss, Server, Network)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricDetailItem(
                        label = "PING",
                        value = reportData.pingMs?.let { "$it ms" } ?: "--",
                        valueColor = NeonBlue
                    )
                    MetricDetailItem(
                        label = "JITTER",
                        value = "${reportData.jitterMs ?: 2} ms",
                        valueColor = Color(0xFFFFB74D)
                    )
                    MetricDetailItem(
                        label = "PACKET LOSS",
                        value = reportData.packetLossPercent?.let { String.format(Locale.US, "%.1f%%", it) } ?: "--",
                        valueColor = Color(0xFF64B5F6)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SERVER: ${reportData.serverName}",
                        color = CyberMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (!reportData.publicIp.isNullOrBlank()) {
                        Text(
                            text = "IP: ${reportData.publicIp}",
                            color = Color(0xFFA9C6FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Text(
                text = stringResource(R.string.str_share_options_37),
                color = CyberMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Button 1: System Share (LINE, Messenger, Socials)
            ShareActionButton(
                icon = Icons.Default.Share,
                iconColor = NeonBlue,
                title = stringResource(R.string.str_share_via_app_line_social_chat_38),
                description = stringResource(R.string.str_send_formatted_summary_to_mess_39),
                onClick = {
                    doShareIntent(shareTextSummary)
                    onDismiss()
                },
                testTag = "share_intent_button"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Button 2: Copy Text to Clipboard
            ShareActionButton(
                icon = Icons.Default.ContentCopy,
                iconColor = NeonGreen,
                title = stringResource(R.string.str_copy_detailed_summary_40),
                description = stringResource(R.string.str_copy_text_report_to_clipboard_41),
                onClick = {
                    copyToClipboard(shareTextSummary)
                    onDismiss()
                },
                testTag = "copy_summary_button"
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MetricDetailItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = CyberMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun ShareActionButton(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberPanel)
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = iconColor)
            ) { onClick() }
            .padding(14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = CyberInk,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = CyberMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
