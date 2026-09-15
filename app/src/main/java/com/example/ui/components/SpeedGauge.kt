package com.example.ui.components

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeedUnit
import com.example.model.TestPhase
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Maps real-time network speed (in Mbps) to a normalized dial fraction [0f..1f].
 * Uses a progressive logarithmic curve for maximum visual precision from 0 to 1000+ Mbps.
 */
private fun calculateSpeedFraction(speed: Double): Float {
    if (speed <= 0.001) return 0f
    return when {
        speed < 10.0 -> (speed / 10.0).toFloat() * 0.15f
        speed < 50.0 -> 0.15f + ((speed - 10.0) / 40.0).toFloat() * 0.20f
        speed < 100.0 -> 0.35f + ((speed - 50.0) / 50.0).toFloat() * 0.20f
        speed < 250.0 -> 0.55f + ((speed - 100.0) / 150.0).toFloat() * 0.15f
        speed < 500.0 -> 0.70f + ((speed - 250.0) / 250.0).toFloat() * 0.15f
        speed < 1000.0 -> 0.85f + ((speed - 500.0) / 500.0).toFloat() * 0.15f
        else -> 1.0f
    }.coerceIn(0f, 1f)
}

@Composable
fun SpeedGauge(
    speedValue: Double,
    progressFraction: Float,
    speedUnit: SpeedUnit,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    isTesting: Boolean = false,
    phase: TestPhase = TestPhase.IDLE,
    isVipAdFree: Boolean = false,
    onOpenVipModal: (() -> Unit)? = null,
    onToggleUnit: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val theme = LocalAppTheme.current
    val isDark = theme.isDark

    // State for AdMob overlay covering the gauge after test completion
    var isGaugeAdDismissed by remember(phase) { mutableStateOf(false) }
    val showGaugeAd = phase == TestPhase.COMPLETED && !isVipAdFree && !isGaugeAdDismissed

    val vibrator = remember(context) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        }.getOrNull()
    }

    val triggerHaptic = remember(hapticFeedback) {
        { _: Int ->
            try {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Throwable) {}
        }
    }

    LaunchedEffect(phase) {
        when (phase) {
            TestPhase.TESTING_PING, TestPhase.TESTING_DOWNLOAD, TestPhase.TESTING_UPLOAD, TestPhase.COMPLETED -> triggerHaptic(1)
            else -> {}
        }
    }

    val targetNeedleFraction = calculateSpeedFraction(speedValue)

    val needleSpringSpec: AnimationSpec<Float> = remember(isTesting, reducedMotion) {
        if (reducedMotion) {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
                visibilityThreshold = 0.0001f
            )
        } else {
            spring(
                dampingRatio = if (isTesting) 0.72f else 0.84f,
                stiffness = if (isTesting) 360f else 260f,
                visibilityThreshold = 0.0001f
            )
        }
    }

    val animatedNeedleFraction by animateFloatAsState(
        targetValue = targetNeedleFraction,
        animationSpec = needleSpringSpec,
        visibilityThreshold = 0.0001f,
        label = "animatedNeedleFraction"
    )

    val animatedSpeed by animateFloatAsState(
        targetValue = speedValue.toFloat(),
        animationSpec = if (reducedMotion) {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
                visibilityThreshold = 0.05f
            )
        } else {
            spring(
                dampingRatio = if (isTesting) 0.75f else 0.86f,
                stiffness = if (isTesting) 360f else 260f,
                visibilityThreshold = 0.05f
            )
        },
        visibilityThreshold = 0.05f,
        label = "animatedSpeed"
    )

    // Dynamic color lerp: Cyan (0) -> Purple (mid) -> Magenta/Pink (max)
    // Directly computed from smoothly animated speed to avoid redundant continuous animator churn
    val speedRatio = (animatedSpeed / 150f).coerceIn(0f, 1f)
    val animatedGaugeColor = when {
        speedRatio < 0.5f -> lerp(Color(0xFF00FFD1), NeonPurple, speedRatio * 2f)
        else -> lerp(NeonPurple, Color(0xFFFF007F), (speedRatio - 0.5f) * 2f)
    }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val textPaint = remember(isDark, density) {
        Paint().apply {
            color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            alpha = if (isDark) 160 else 180
            textSize = with(density) { 9.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }
    val needleShadowPath = remember { Path() }
    val needleBodyPath = remember { Path() }

    val displaySpeed = if (speedUnit == SpeedUnit.MB_S) animatedSpeed / 8.0 else animatedSpeed.toDouble()
    val formattedSpeedStr = String.format(Locale.US, "%.1f", displaySpeed)

    Box(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .aspectRatio(1.0f),
        contentAlignment = Alignment.Center
    ) {
        // Subtle Radial Background Glow
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            animatedGaugeColor.copy(alpha = if (isDark) 0.20f else 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Symmetrical Canvas for Speedometer Gauge (Dial & Needle)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("speed_gauge_canvas")
        ) {
            val centerOffset = this.center
            val strokeWidth = 12.dp.toPx()

            // Exact 270-degree arc: Starts at 135° (bottom-left) and sweeps 270° to 405° / 45° (bottom-right)
            // Leaves an exact 90-degree gap symmetrically centered straight down at 90°.
            val startAngle = 135f
            val sweepAngle = 270f
            val arcRadius = (size.minDimension / 2f) - strokeWidth - 18.dp.toPx()

            // 1. Outer Concentric Precision Track (Subtle decorative ring)
            drawCircle(
                color = if (isDark) Color(0x18FFFFFF) else Color(0x12000000),
                radius = arcRadius + 14.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )

            // 2. Base Background Arc Track
            drawArc(
                color = if (isDark) Color(0x1AFFFFFF) else Color(0x1E000000),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 3. Symmetrical Scale Tick Marks & Speed Numerals
            val totalTicks = 36
            val majorInterval = 6 // 7 major tick marks: 0, 20, 50, 100, 250, 500, 1000
            val speedLabels = listOf("0", "20", "50", "100", "250", "500", "1K")

            val effectiveNeedleFraction = animatedNeedleFraction.coerceIn(0f, 1f)
            val currentNeedleAngle = startAngle + (effectiveNeedleFraction * sweepAngle)

            for (i in 0..totalTicks) {
                val fraction = i.toFloat() / totalTicks
                val tickAngle = startAngle + (fraction * sweepAngle)
                val tickRad = Math.toRadians(tickAngle.toDouble())
                val cosT = cos(tickRad).toFloat()
                val sinT = sin(tickRad).toFloat()

                val isMajor = i % majorInterval == 0
                val tickLength = if (isMajor) 9.dp.toPx() else 4.dp.toPx()
                val tickThickness = if (isMajor) 2.2.dp.toPx() else 1.2.dp.toPx()

                val outerR = arcRadius - (strokeWidth / 2f) - 2.dp.toPx()
                val innerR = outerR - tickLength

                val isPassed = tickAngle <= currentNeedleAngle + 0.5f
                val tickColor = if (isPassed) {
                    animatedGaugeColor.copy(alpha = if (isMajor) 0.95f else 0.65f)
                } else {
                    if (isDark) Color(0x2EFFFFFF) else Color(0x28000000)
                }

                drawLine(
                    color = tickColor,
                    start = Offset(centerOffset.x + innerR * cosT, centerOffset.y + innerR * sinT),
                    end = Offset(centerOffset.x + outerR * cosT, centerOffset.y + outerR * sinT),
                    strokeWidth = tickThickness,
                    cap = StrokeCap.Round
                )

                // Draw Symmetrical Speed Numbers on Major Ticks
                if (isMajor) {
                    val labelIndex = i / majorInterval
                    if (labelIndex < speedLabels.size) {
                        val textR = innerR - 10.dp.toPx()
                        val textX = centerOffset.x + textR * cosT
                        val textY = centerOffset.y + textR * sinT + (textPaint.textSize / 3f)
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(speedLabels[labelIndex], textX, textY, textPaint)
                        }
                    }
                }
            }

            // 4. Dynamic Speed Progress Arc (Active sweep with Neon Gradient)
            val activeSweep = (animatedNeedleFraction * sweepAngle).coerceAtLeast(0.5f)
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF00FFD1),
                        animatedGaugeColor,
                        Color(0xFFFF007F),
                        animatedGaugeColor
                    ),
                    center = centerOffset
                ),
                startAngle = startAngle,
                sweepAngle = activeSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 5. Perfectly Symmetrical Supercar Needle Anchored from Center Hub
            val needleRad = Math.toRadians(currentNeedleAngle.toDouble())
            val cosA = cos(needleRad).toFloat()
            val sinA = sin(needleRad).toFloat()
            val perpCos = -sinA
            val perpSin = cosA

            val needleTipRadius = arcRadius - (strokeWidth / 2f) - 1.dp.toPx()
            val needleShoulderRadius = arcRadius * 0.65f
            val needleTailRadius = 24.dp.toPx()

            val hubWidth = 4.5.dp.toPx()
            val shoulderWidth = 2.0.dp.toPx()
            val tailWidth = 3.5.dp.toPx()

            // Needle Polygon Vertices
            val tipPoint = Offset(centerOffset.x + needleTipRadius * cosA, centerOffset.y + needleTipRadius * sinA)
            val shoulderLeft = Offset(
                centerOffset.x + needleShoulderRadius * cosA + shoulderWidth * perpCos,
                centerOffset.y + needleShoulderRadius * sinA + shoulderWidth * perpSin
            )
            val shoulderRight = Offset(
                centerOffset.x + needleShoulderRadius * cosA - shoulderWidth * perpCos,
                centerOffset.y + needleShoulderRadius * sinA - shoulderWidth * perpSin
            )
            val hubLeft = Offset(
                centerOffset.x + hubWidth * perpCos,
                centerOffset.y + hubWidth * perpSin
            )
            val hubRight = Offset(
                centerOffset.x - hubWidth * perpCos,
                centerOffset.y - hubWidth * perpSin
            )
            val tailPoint = Offset(
                centerOffset.x - needleTailRadius * cosA,
                centerOffset.y - needleTailRadius * sinA
            )

            // Needle Shadow
            val shadowOffset = 3.dp.toPx()
            needleShadowPath.reset()
            needleShadowPath.moveTo(tailPoint.x + shadowOffset, tailPoint.y + shadowOffset)
            needleShadowPath.lineTo(hubLeft.x + shadowOffset, hubLeft.y + shadowOffset)
            needleShadowPath.lineTo(shoulderLeft.x + shadowOffset, shoulderLeft.y + shadowOffset)
            needleShadowPath.lineTo(tipPoint.x + shadowOffset, tipPoint.y + shadowOffset)
            needleShadowPath.lineTo(shoulderRight.x + shadowOffset, shoulderRight.y + shadowOffset)
            needleShadowPath.lineTo(hubRight.x + shadowOffset, hubRight.y + shadowOffset)
            needleShadowPath.close()
            drawPath(needleShadowPath, color = Color(0x38000000))

            // Needle Body with Radiant Neon Gradient
            needleBodyPath.reset()
            needleBodyPath.moveTo(tailPoint.x, tailPoint.y)
            needleBodyPath.lineTo(hubLeft.x, hubLeft.y)
            needleBodyPath.lineTo(shoulderLeft.x, shoulderLeft.y)
            needleBodyPath.lineTo(tipPoint.x, tipPoint.y)
            needleBodyPath.lineTo(shoulderRight.x, shoulderRight.y)
            needleBodyPath.lineTo(hubRight.x, hubRight.y)
            needleBodyPath.close()

            drawPath(
                path = needleBodyPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        animatedGaugeColor.copy(alpha = 0.6f),
                        animatedGaugeColor,
                        Color.White
                    ),
                    start = tailPoint,
                    end = tipPoint
                )
            )

            // Needle Glowing Pointer Bead
            drawCircle(
                color = animatedGaugeColor.copy(alpha = 0.45f),
                radius = 6.dp.toPx(),
                center = tipPoint
            )
            drawCircle(
                color = Color.White,
                radius = 2.5.dp.toPx(),
                center = tipPoint
            )

            // 6. Central Metallic Hub Cap (Bezel & Pivot)
            drawCircle(
                color = animatedGaugeColor.copy(alpha = 0.35f),
                radius = 16.dp.toPx(),
                center = centerOffset
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isDark) Color(0xFF263248) else Color(0xFFCBD5E1),
                        if (isDark) Color(0xFF0F1522) else Color(0xFF64748B)
                    ),
                    center = centerOffset,
                    radius = 12.dp.toPx()
                ),
                radius = 12.dp.toPx(),
                center = centerOffset
            )
            drawCircle(
                color = animatedGaugeColor,
                radius = 4.dp.toPx(),
                center = centerOffset
            )
            drawCircle(
                color = Color.White,
                radius = 1.5.dp.toPx(),
                center = centerOffset
            )
        }

        // Center Digital Readout (Speed Number, Unit Toggle & Status Pill)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 28.dp)
        ) {
            // Speed Phase Status Badge
            val phaseLabel = when (phase) {
                TestPhase.TESTING_PING -> "PING"
                TestPhase.TESTING_DOWNLOAD -> "DOWNLOAD"
                TestPhase.TESTING_UPLOAD -> "UPLOAD"
                TestPhase.COMPLETED -> "DONE"
                else -> if (isTesting) "LIVE" else "READY"
            }
            val phaseBadgeColor = when (phase) {
                TestPhase.COMPLETED -> Color(0xFF34C759)
                TestPhase.TESTING_DOWNLOAD -> Color(0xFF00FFD1)
                TestPhase.TESTING_UPLOAD -> Color(0xFFFF007F)
                else -> theme.colors.textMuted
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(phaseBadgeColor.copy(alpha = 0.15f))
                    .border(1.dp, phaseBadgeColor.copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = phaseLabel,
                    color = phaseBadgeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Speed Value Text (High-contrast, fully visible in Dark and Light mode)
            Text(
                text = formattedSpeedStr,
                color = theme.colors.textMain,
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.5).sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("speed_value_text")
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Speed Unit Toggle Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(animatedGaugeColor.copy(alpha = 0.12f))
                    .border(1.dp, animatedGaugeColor.copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                    .clickable(enabled = onToggleUnit != null && !isTesting) {
                        triggerHaptic(1)
                        onToggleUnit?.invoke()
                    }
                    .padding(horizontal = 10.dp, vertical = 3.dp)
                    .testTag("speed_unit_toggle")
            ) {
                Text(
                    text = speedUnit.label,
                    color = animatedGaugeColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.testTag("speed_unit_text")
                )
            }
        }

        // =========================================================================
        // AdMob Overlay Covering the Gauge Upon Test Completion ("เวลาเทสเสร็จ admod จะโชวปิดเรือนไมล์")
        // =========================================================================
        AnimatedVisibility(
            visible = showGaugeAd,
            enter = fadeIn(tween(300)) + scaleIn(tween(300)),
            exit = fadeOut(tween(250)) + scaleOut(tween(250)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xF50D1322) else Color(0xF5FFFFFF))
                    .border(1.5.dp, Color(0xFF00FFD1).copy(alpha = 0.60f), CircleShape)
                    .padding(20.dp)
                    .testTag("gauge_admob_overlay"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Top Ad Header & Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFB300).copy(alpha = 0.20f))
                                .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AdMob Sponsored",
                                color = Color(0xFFFFB300),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Close Ad button to see the gauge dial (accessible 48dp touch target)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { isGaugeAdDismissed = true }
                                .testTag("dismiss_gauge_ad_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0x44FFFFFF) else Color(0x33000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Ad",
                                    tint = theme.colors.textMain,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "AIS 5G Fibre Speed Boost",
                        color = theme.colors.textMain,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "สปีดแรงเต็มพิกัด 1000/1000 Mbps พร้อมเราเตอร์ WiFi 6 Tri-Band ลื่นไหลทุกการใช้งาน",
                        color = theme.colors.textMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Upgrade to VIP Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFFFB300), Color(0xFFFF8F00))))
                            .clickable { onOpenVipModal?.invoke() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("upgrade_vip_from_gauge_ad"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "VIP",
                                tint = Color(0xFF1E1400),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "ปิดโฆษณาถาวรด้วย VIP",
                                color = Color(0xFF1E1400),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Prominent Dismiss Button to view speedometer
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isDark) Color(0x22FFFFFF) else Color(0x14000000))
                            .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0x22000000), RoundedCornerShape(20.dp))
                            .clickable { isGaugeAdDismissed = true }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("dismiss_prompt_btn")
                    ) {
                        Text(
                            text = "✕ ปิดโฆษณา (ดูเรือนไมล์)",
                            color = theme.colors.textMain,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
