package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.SpeedTestState
import com.example.ui.theme.LocalAppTheme

enum class ExperienceRating(
    val label: String,
    val stars: Int,
    val badgeColor: Color
) {
    EXCELLENT("EXCELLENT", 5, Color(0xFF34C759)),
    VERY_GOOD("VERY GOOD", 4, Color(0xFF00FFD1)),
    GOOD("GOOD", 4, Color(0xFF007AFF)),
    FAIR("FAIR", 3, Color(0xFFFF9500)),
    POOR("POOR", 2, Color(0xFFFF3B30))
}

@Composable
fun ExperienceAssessmentView(
    testState: SpeedTestState,
    language: Language,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val isDark = theme.isDark

    val dl = testState.downloadMbps
    val ul = testState.uploadMbps
    val ping = testState.pingMs

    // Never invent an experience rating. A rating exists only when its required
    // measured inputs are available from the current test.
    val browsingRating = if (dl != null && ping != null) {
        when {
            dl >= 50.0 && ping <= 50 -> ExperienceRating.GOOD
            dl >= 20.0 -> ExperienceRating.GOOD
            else -> ExperienceRating.FAIR
        }
    } else null

    val gamingRating = if (dl != null && ping != null) {
        when {
            ping <= 40 && dl >= 30.0 -> ExperienceRating.EXCELLENT
            ping <= 70 -> ExperienceRating.VERY_GOOD
            else -> ExperienceRating.GOOD
        }
    } else null

    val videoRating = if (dl != null) {
        when {
            dl >= 100.0 -> ExperienceRating.VERY_GOOD
            dl >= 30.0 -> ExperienceRating.GOOD
            else -> ExperienceRating.FAIR
        }
    } else null

    val videoCallRating = if (ul != null && dl != null && ping != null) {
        when {
            ul >= 20.0 && dl >= 20.0 && ping <= 60 -> ExperienceRating.GOOD
            ul >= 10.0 -> ExperienceRating.GOOD
            else -> ExperienceRating.FAIR
        }
    } else null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("experience_assessment_view")
    ) {
        // Section Header
        Text(
            text = if (language == Language.TH) "ประเมินประสบการณ์การใช้งาน" else "Experience Assessment",
            color = theme.colors.textMain,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
        )

        // 2x2 Symmetrical Grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Row 1: Browsing & Gaming
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AssessmentCard(
                    name = "Browsing",
                    icon = Icons.Default.Language,
                    rating = browsingRating,
                    iconTint = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )

                AssessmentCard(
                    name = "Gaming",
                    icon = Icons.Default.SportsEsports,
                    rating = gamingRating,
                    iconTint = Color(0xFF34C759),
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Video & Video Call
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AssessmentCard(
                    name = "Video",
                    icon = Icons.Default.PlayCircle,
                    rating = videoRating,
                    iconTint = Color(0xFFAF52DE),
                    modifier = Modifier.weight(1f)
                )

                AssessmentCard(
                    name = "Video Call",
                    icon = Icons.Default.Videocam,
                    rating = videoCallRating,
                    iconTint = Color(0xFF00FFD1),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AssessmentCard(
    name: String,
    icon: ImageVector,
    rating: ExperienceRating?,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val isDark = theme.isDark

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isDark) Color(0xFF131824).copy(alpha = 0.85f) else Color(0xFFFFFFFF)
            )
            .border(
                1.dp,
                if (isDark) Color(0x33FFFFFF) else Color(0xFFE2E8F0),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            // Icon & Name Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = name,
                    color = theme.colors.textMain,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (rating == null) {
                Text(
                    text = "--",
                    color = theme.colors.textMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                // Stars are rendered only from measured results.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..5) {
                        val isFilled = i <= rating.stars
                        Text(
                            text = "★",
                            color = if (isFilled) Color(0xFFFBBF24) else Color(0x44FFFFFF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(rating.badgeColor.copy(alpha = 0.16f))
                        .border(1.dp, rating.badgeColor.copy(alpha = 0.40f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = rating.label,
                        color = rating.badgeColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
