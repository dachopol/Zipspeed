package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.SpeedTestState
import com.example.model.TestPhase
import com.example.ui.theme.CyberInk
import com.example.ui.theme.CyberMuted
import com.example.ui.theme.CyberPanel
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRose

enum class ExperienceCategory(
    val id: String,
    val titleTh: String,
    val titleEn: String,
    val descriptionTh: String,
    val descriptionEn: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    BROWSING(
        id = "browsing",
        titleTh = "ท่องเว็บ",
        titleEn = "Browsing",
        descriptionTh = "วัดว่าจัดการงานพื้นฐานอย่างเช็คอีเมล เล่นโซเชียลได้ดีแค่ไหน",
        descriptionEn = "Measures how well basic tasks like checking email and browsing social media are handled.",
        icon = Icons.Default.Language,
        accentColor = NeonBlue
    ),
    GAMING(
        id = "gaming",
        titleTh = "เล่นเกม",
        titleEn = "Gaming",
        descriptionTh = "ขึ้นอยู่กับความเร็วที่เหมาะสม บวกกับ ping ต้องเร็ว ไม่งั้นจะแลค",
        descriptionEn = "Depends on adequate speed plus low ping/latency, otherwise lag occurs.",
        icon = Icons.Default.SportsEsports,
        accentColor = Color(0xFFFFB74D)
    ),
    STREAMING(
        id = "streaming",
        titleTh = "ดูวิดีโอ",
        titleEn = "Video",
        descriptionTh = "ต้องการความเร็วดาวน์โหลดดีๆ โดยเฉพาะคอนเทนต์ความละเอียดสูง",
        descriptionEn = "Requires good download speed, especially for high-resolution content.",
        icon = Icons.Default.PlayCircle,
        accentColor = NeonGreen
    ),
    VIDEO_CALL(
        id = "video_call",
        titleTh = "วิดีโอคอล",
        titleEn = "Video call",
        descriptionTh = "ต้องใช้ทั้งดาวน์โหลดและอัปโหลดดี ถ้าหน่วงเวลาสูงวิดีโอจะไม่ซิงค์กัน",
        descriptionEn = "Requires both good download and upload speeds; high latency causes audio and video to desync.",
        icon = Icons.Default.Videocam,
        accentColor = NeonPurple
    )
}

/**
 * Calculates score dots (1 to 5) based on network metrics.
 */
fun calculateExperienceDots(
    category: ExperienceCategory,
    downloadMbps: Double?,
    uploadMbps: Double?,
    pingMs: Int?,
    jitterMs: Int?
): Int {
    val dl = downloadMbps ?: 25.0
    val ul = uploadMbps ?: 10.0
    val ping = pingMs ?: 20
    val jitter = jitterMs ?: 3

    return when (category) {
        ExperienceCategory.BROWSING -> {
            when {
                dl >= 20.0 && ping <= 40 -> 5
                dl >= 10.0 && ping <= 70 -> 4
                dl >= 5.0 && ping <= 120 -> 3
                dl >= 2.0 -> 2
                else -> 1
            }
        }
        ExperienceCategory.GAMING -> {
            when {
                ping <= 20 && jitter <= 4 && dl >= 15.0 -> 5
                ping <= 45 && jitter <= 10 && dl >= 10.0 -> 4
                ping <= 80 && jitter <= 20 -> 3
                ping <= 120 -> 2
                else -> 1
            }
        }
        ExperienceCategory.STREAMING -> {
            when {
                dl >= 50.0 -> 5 // 4K UHD
                dl >= 25.0 -> 4 // 1440p QHD
                dl >= 10.0 -> 3 // 1080p FHD
                dl >= 5.0 -> 2  // 720p HD
                else -> 1       // 480p SD
            }
        }
        ExperienceCategory.VIDEO_CALL -> {
            when {
                dl >= 15.0 && ul >= 8.0 && ping <= 50 -> 5 // HD sync
                dl >= 8.0 && ul >= 4.0 && ping <= 75 -> 4
                dl >= 4.0 && ul >= 2.0 && ping <= 100 -> 3
                dl >= 2.0 && ul >= 1.0 -> 2
                else -> 1
            }
        }
    }
}

/**
 * Centered Experience Assessment Row / Grid with 4 category icons and 5-dot score ratings.
 * Tapping or clicking any category displays an explanatory card with the exact requested description.
 */
@Composable
fun ExperienceAssessmentView(
    testState: SpeedTestState,
    language: Language,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<ExperienceCategory?>(null) }

    val dl = testState.downloadMbps
    val ul = testState.uploadMbps
    val ping = testState.pingMs
    val jitter = testState.jitterMs

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CyberPanel)
            .border(1.dp, Color(0x1F2F7BFF), RoundedCornerShape(20.dp))
            .padding(14.dp)
            .testTag("experience_assessment_view")
    ) {
        // Section Header
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
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                )
                Text(
                    text = if (language == Language.TH) "การประเมินการใช้งาน" else "Experience Assessment",
                    color = CyberInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = if (language == Language.TH) "แตะไอคอนเพื่อดูคำอธิบาย" else "Tap icon for explanation",
                color = CyberMuted,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4 Centered Assessment Icons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ExperienceCategory.values().forEach { category ->
                val dots = calculateExperienceDots(category, dl, ul, ping, jitter)
                val isSelected = selectedCategory == category

                ExperienceIconItem(
                    category = category,
                    dots = dots,
                    isSelected = isSelected,
                    language = language,
                    onClick = {
                        selectedCategory = if (isSelected) null else category
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Animated Tooltip / Explanation Detail Popup Card
        AnimatedVisibility(
            visible = selectedCategory != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            selectedCategory?.let { category ->
                val dots = calculateExperienceDots(category, dl, ul, ping, jitter)
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF131726))
                        .border(1.dp, category.accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                        .testTag("category_explanation_card")
                ) {
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
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(category.accentColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null,
                                    tint = category.accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (language == Language.TH) category.titleTh else category.titleEn,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { selectedCategory = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = CyberMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Rating Score Dots & Rating Label
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ScoreDotsRow(dots = dots, accentColor = category.accentColor)
                        Text(
                            text = when (dots) {
                                5 -> if (language == Language.TH) "• ยอดเยี่ยม (5/5)" else "• Excellent (5/5)"
                                4 -> if (language == Language.TH) "• ดีมาก (4/5)" else "• Very Good (4/5)"
                                3 -> if (language == Language.TH) "• ดี (3/5)" else "• Good (3/5)"
                                2 -> if (language == Language.TH) "• พอใช้ (2/5)" else "• Fair (2/5)"
                                else -> if (language == Language.TH) "• ช้า (1/5)" else "• Poor (1/5)"
                            },
                            color = category.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Description text verbatim as user requested
                    Text(
                        text = if (language == Language.TH) category.descriptionTh else category.descriptionEn,
                        color = Color(0xFFD6E2FF),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ExperienceIconItem(
    category: ExperienceCategory,
    dots: Int,
    isSelected: Boolean,
    language: Language,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) category.accentColor.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = category.accentColor)
            ) { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag("experience_item_${category.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Category Icon in glowing circular container
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) category.accentColor.copy(alpha = 0.30f)
                    else Color(0x18FFFFFF)
                )
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) category.accentColor else Color(0x22FFFFFF),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = if (language == Language.TH) category.titleTh else category.titleEn,
                tint = if (isSelected) category.accentColor else Color(0xFFD8E4FC),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title
        Text(
            text = if (language == Language.TH) category.titleTh else category.titleEn,
            color = if (isSelected) category.accentColor else CyberInk,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 5 Score Dots (Speedtest style)
        ScoreDotsRow(
            dots = dots,
            accentColor = category.accentColor,
            dotSize = 5.dp
        )
    }
}

@Composable
fun ScoreDotsRow(
    dots: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
    dotSize: androidx.compose.ui.unit.Dp = 6.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            val filled = i <= dots
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(if (filled) accentColor else Color(0x33FFFFFF))
            )
        }
    }
}
