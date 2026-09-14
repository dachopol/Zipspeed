package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.SpeedTestState
import com.example.model.SpeedUnit
import com.example.ui.theme.CyberInk
import com.example.ui.theme.CyberMuted
import com.example.ui.theme.CyberPanel
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.util.startActivitySafely
import java.net.URLEncoder
import java.util.Locale

@Composable
fun QuickShareNavCard(
    testState: SpeedTestState,
    speedUnit: SpeedUnit,
    language: Language,
    onNavigateToResults: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenFullShareModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val dlVal = testState.downloadMbps?.let {
        if (speedUnit == SpeedUnit.MB_S) it / 8.0 else it
    }
    val ulVal = testState.uploadMbps?.let {
        if (speedUnit == SpeedUnit.MB_S) it / 8.0 else it
    }
    val dlStr = dlVal?.let { String.format(Locale.US, "%.1f", it) } ?: "—"
    val ulStr = ulVal?.let { String.format(Locale.US, "%.1f", it) } ?: "—"
    val pingStr = testState.pingMs?.let { "$it ms" } ?: "—"

    val shareText = remember(dlStr, ulStr, pingStr, speedUnit) {
        "My Speedtest Result: Download: $dlStr ${speedUnit.label}, Upload: $ulStr ${speedUnit.label}, Ping: $pingStr via Zipspeed"
    }

    val shareUrl = "https://www.speedtest.net"

    fun shareToX() {
        try {
            val encodedText = URLEncoder.encode("$shareText\n$shareUrl", "UTF-8")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?text=$encodedText"))
            context.startActivitySafely(intent)
        } catch (_: Exception) {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "$shareText\n$shareUrl")
            }
            context.startActivitySafely(Intent.createChooser(sendIntent, "Share to X"))
        }
    }

    fun shareToFacebook() {
        try {
            val encodedUrl = URLEncoder.encode(shareUrl, "UTF-8")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/sharer/sharer.php?u=$encodedUrl"))
            context.startActivitySafely(intent)
        } catch (_: Exception) {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "$shareText\n$shareUrl")
            }
            context.startActivitySafely(Intent.createChooser(sendIntent, "Share to Facebook"))
        }
    }

    fun copyLink() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Speedtest Result", "$shareText\n$shareUrl")
        clipboard.setPrimaryClip(clip)
        Toast.makeText(
            context,
            if (language == Language.TH) "คัดลอกลิงก์ผลทดสอบแล้ว!" else "Result link copied to clipboard!",
            Toast.LENGTH_SHORT
        ).show()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CyberPanel)
            .border(1.dp, Color(0x1F2F7BFF), RoundedCornerShape(20.dp))
            .padding(14.dp)
            .testTag("quick_share_nav_card")
    ) {
        // Quick Navigation Buttons: RESULTS and SETTINGS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // RESULTS Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1A2F7BFF))
                    .border(1.dp, Color(0x442F7BFF), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = NeonBlue)
                    ) { onNavigateToResults() }
                    .padding(vertical = 10.dp, horizontal = 8.dp)
                    .testTag("btn_quick_results"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Results",
                        tint = NeonBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (language == Language.TH) "RESULTS (ดูประวัติ)" else "RESULTS",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // SETTINGS Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1AFFFFFF))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White)
                    ) { onNavigateToSettings() }
                    .padding(vertical = 10.dp, horizontal = 8.dp)
                    .testTag("btn_quick_settings"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = CyberMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (language == Language.TH) "SETTINGS (ตั้งค่า)" else "SETTINGS",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Social Share Section
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
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = NeonPurple,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (language == Language.TH) "แชร์ผลการทดสอบ" else "Share Results",
                    color = CyberInk,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = if (language == Language.TH) "แชร์ผ่านลิงก์" else "via Link",
                color = CyberMuted,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Social Buttons Row: X, Facebook, Copy Link, Full Report
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // X (Twitter)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF000000))
                    .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White)
                    ) { shareToX() }
                    .padding(vertical = 8.dp)
                    .testTag("share_btn_x"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "X Post",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Facebook
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1877F2))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White)
                    ) { shareToFacebook() }
                    .padding(vertical = 8.dp)
                    .testTag("share_btn_facebook"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Facebook",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Copy Link
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x182F7BFF))
                    .border(1.dp, Color(0x332F7BFF), RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = NeonBlue)
                    ) { copyLink() }
                    .padding(vertical = 8.dp)
                    .testTag("share_btn_copy_link"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Link",
                        tint = NeonBlue,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (language == Language.TH) "คัดลอก" else "Copy",
                        color = Color(0xFFA9C6FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // More Options (Detailed Report)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x15FFFFFF))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White)
                    ) { onOpenFullShareModal() }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("share_btn_more"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "•••",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
