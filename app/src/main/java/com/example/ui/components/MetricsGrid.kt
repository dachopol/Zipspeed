package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.SpeedTestState
import com.example.model.SpeedUnit
import com.example.ui.theme.CyberPanel
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ZipspeedPanel
import com.example.ui.theme.ZipspeedTextPrimary
import com.example.ui.theme.ZipspeedTextPrimary
import java.util.Locale

@Composable
fun MetricsGrid(
    testState: SpeedTestState,
    speedUnit: SpeedUnit,
    language: Language,
    isProPlan: Boolean = true,
    modifier: Modifier = Modifier
) {
    val downloadVal = testState.downloadMbps?.let {
        if (speedUnit == SpeedUnit.MB_S) it / 8.0 else it
    }
    val downloadText = downloadVal?.let { String.format(Locale.US, "%.1f", it) } ?: "—"

    val uploadVal = testState.uploadMbps?.let {
        if (speedUnit == SpeedUnit.MB_S) it / 8.0 else it
    }
    val uploadText = uploadVal?.let { String.format(Locale.US, "%.1f", it) } ?: "—"

    val pingText = testState.pingMs?.toString() ?: "—"
    val jitterText = testState.jitterMs?.toString() ?: "—"

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // TOP ROW: Download and Upload
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // DOWNLOAD CARD
            BigMetricCard(
                label = "Download", // Or stringResource(R.string.str_download_43) if you prefer
                value = downloadText,
                unit = speedUnit.label,
                isDownload = true,
                modifier = Modifier.weight(1f).testTag("metric_download")
            )

            // UPLOAD CARD
            BigMetricCard(
                label = "Upload", // Or stringResource(R.string.str_upload_44)
                value = uploadText,
                unit = speedUnit.label,
                isDownload = false,
                modifier = Modifier.weight(1f).testTag("metric_upload")
            )
        }

        // SECOND ROW: Ping and Jitter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ZipspeedPanel) // Dark blue-ish background from image
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PING
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer, // Assuming we have some clock/timer icon, using ic_speed for now or a material icon
                    contentDescription = null,
                    tint = ZipspeedTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = buildAnnotatedString {
                        append("Ping ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = ZipspeedTextPrimary)) {
                            append("$pingText ms")
                        }
                    },
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = ZipspeedTextPrimary
                )
            }

            // DIVIDER
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(18.dp)
                    .background(Color(0x33FFFFFF))
            )

            // JITTER
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timeline, // Assuming some wave icon, using ic_chart for now
                    contentDescription = null,
                    tint = ZipspeedTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = buildAnnotatedString {
                        append("Jitter ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = ZipspeedTextPrimary)) {
                            append("$jitterText ms")
                        }
                    },
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = ZipspeedTextPrimary
                )
            }
        }
    }
}

@Composable
private fun BigMetricCard(
    label: String,
    value: String,
    unit: String,
    isDownload: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ZipspeedPanel) // Dark blue-ish background matching the image
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label (.label-th)
        Text(
            text = label,
            fontFamily = FontFamily.SansSerif,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = ZipspeedTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Big Number and Unit Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(1.dp, ZipspeedTextPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDownload) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    tint = ZipspeedTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Big Number (.number-big)
            Text(
                text = value,
                fontFamily = FontFamily.SansSerif, // Can't easily use Saira/Kanit if not bundled, but SansSerif matches closest available
                fontSize = 52.sp,
                fontWeight = FontWeight.Light,
                lineHeight = 52.sp,
                color = ZipspeedTextPrimary,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Unit (.unit)
            Text(
                text = unit.uppercase(Locale.getDefault()),
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ZipspeedTextPrimary
            )
        }
    }
}
