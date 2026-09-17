package com.example.ui.components

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeedUnit
import com.example.model.TestPhase
import com.example.ui.theme.ChampagneDark
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ChampagneLight
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.WinePink
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Maps real-time network speed (in Mbps) to normalized dial fraction [0f..1f].
 * Essential tick milestones: 0, 50, 100, 200, 500, 1000 Mbps.
 */
private fun calculateSpeedFraction(speed: Double): Float {
    if (speed <= 0.001) return 0f
    return when {
        speed < 50.0 -> (speed / 50.0).toFloat() * 0.20f
        speed < 100.0 -> 0.20f + ((speed - 50.0) / 50.0).toFloat() * 0.20f
        speed < 200.0 -> 0.40f + ((speed - 100.0) / 100.0).toFloat() * 0.20f
        speed < 500.0 -> 0.60f + ((speed - 200.0) / 300.0).toFloat() * 0.20f
        speed < 1000.0 -> 0.80f + ((speed - 500.0) / 500.0).toFloat() * 0.20f
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
    onToggleUnit: (() -> Unit)? = null,
    onStartTest: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val theme = LocalAppTheme.current
    val isDark = theme.isDark

    // State for AdMob overlay covering gauge on completion
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

    // Convert display speed if unit is MB/s
    val displayedSpeed = if (speedUnit == SpeedUnit.MB_S) speedValue / 8.0 else speedValue
    val rawTargetFraction = calculateSpeedFraction(speedValue)

    // Smooth animation with reduced-motion support
    val animationSpec: AnimationSpec<Float> = if (reducedMotion) {
        tween(durationMillis = 0)
    } else {
        spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessLow)
    }

    val animatedNeedleFraction by animateFloatAsState(
        targetValue = rawTargetFraction,
        animationSpec = animationSpec,
        label = "needleMotion"
    )

    // Milestone haptic trigger
    LaunchedEffect((animatedNeedleFraction * 5).toInt()) {
        if (isTesting && !reducedMotion) {
            triggerHaptic(1)
        }
    }

    // Gentle pulse animation for the center GO button when idle
    val infiniteTransition = rememberInfiniteTransition(label = "goPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Gauge Tick Text Paint
    val tickTextPaint = remember(isDark) {
        Paint().apply {
            color = if (isDark) android.graphics.Color.parseColor("#A7B3C6") else android.graphics.Color.parseColor("#64748B")
            textSize = 28f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            isAntiAlias = true
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 300.dp)
            .aspectRatio(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (!isTesting) {
                        onStartTest?.invoke()
                    }
                }
            )
            .testTag("speed_gauge_container"),
        contentAlignment = Alignment.Center
    ) {
        val gaugeSize = maxWidth

        // 3D Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("speed_gauge_canvas")
        ) {
            val centerOffset = this.center
            val radius = size.minDimension / 2f
            val bezelWidth = 7.dp.toPx()
            val strokeWidth = 9.dp.toPx()

            // 1. Outer 3D Matte Champagne Bezel with Top Highlight and Bottom Shadow
            val bezelBrush = Brush.verticalGradient(
                colors = listOf(
                    ChampagneLight, // Top Specular Highlight
                    ChampagneGold,  // Midtone Matte Gold
                    ChampagneDark,  // Lower Bronze
                    Color(0xFF4A3B22) // Bottom Shadow
                ),
                startY = 0f,
                endY = size.height
            )

            drawCircle(
                brush = bezelBrush,
                radius = radius - 2.dp.toPx(),
                style = Stroke(width = bezelWidth)
            )

            // Inner Bevel Shadow Ring (Deep tactile sunken rim)
            drawCircle(
                color = if (isDark) Color(0x66000000) else Color(0x22000000),
                radius = radius - bezelWidth - 1.dp.toPx(),
                style = Stroke(width = 2.dp.toPx())
            )

            // 2. Deep Navy Concave Dial Face (Subtle Radial Depth)
            val dialFaceBrush = Brush.radialGradient(
                colors = if (isDark) {
                    listOf(Color(0xFF131F33), DeepNavy)
                } else {
                    listOf(Color(0xFFFBFBF9), Color(0xFFEDEAE3))
                },
                center = centerOffset,
                radius = radius - bezelWidth
            )

            drawCircle(
                brush = dialFaceBrush,
                radius = radius - bezelWidth - 2.dp.toPx()
            )

            // 3. Arc Configuration (270° Sweep: from 135° to 405° with 90° bottom gap)
            val startAngle = 135f
            val sweepAngle = 270f
            val arcRadius = radius - bezelWidth - strokeWidth - 12.dp.toPx()

            // Background Arc Track (Muted Track)
            drawArc(
                color = if (isDark) Color(0x18FFFFFF) else Color(0x18000000),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 4. Essential Tick Marks & Milestone Numerals (0, 50, 100, 200, 500, 1000)
            val milestoneTicks = listOf(
                0.00f to "0",
                0.20f to "50",
                0.40f to "100",
                0.60f to "200",
                0.80f to "500",
                1.00f to "1K"
            )

            val effectiveNeedleFraction = animatedNeedleFraction.coerceIn(0f, 1f)
            val currentNeedleAngle = startAngle + (effectiveNeedleFraction * sweepAngle)

            // Draw Minor and Major Ticks
            val totalMinorTicks = 25
            for (i in 0..totalMinorTicks) {
                val frac = i.toFloat() / totalMinorTicks
                val angle = startAngle + (frac * sweepAngle)
                val rad = Math.toRadians(angle.toDouble())
                val cosT = cos(rad).toFloat()
                val sinT = sin(rad).toFloat()

                val outerR = arcRadius - (strokeWidth / 2f) - 2.dp.toPx()
                val innerR = outerR - 4.dp.toPx()

                drawLine(
                    color = if (isDark) Color(0x24FFFFFF) else Color(0x20000000),
                    start = Offset(centerOffset.x + innerR * cosT, centerOffset.y + innerR * sinT),
                    end = Offset(centerOffset.x + outerR * cosT, centerOffset.y + outerR * sinT),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draw Essential Milestones
            milestoneTicks.forEach { (frac, label) ->
                val angle = startAngle + (frac * sweepAngle)
                val rad = Math.toRadians(angle.toDouble())
                val cosT = cos(rad).toFloat()
                val sinT = sin(rad).toFloat()

                val outerR = arcRadius - (strokeWidth / 2f) - 2.dp.toPx()
                val innerR = outerR - 7.dp.toPx()
                val isReached = angle <= currentNeedleAngle + 1f

                // Major tick line
                drawLine(
                    color = if (isReached) ChampagneGold else if (isDark) Color(0x44FFFFFF) else Color(0x38000000),
                    start = Offset(centerOffset.x + innerR * cosT, centerOffset.y + innerR * sinT),
                    end = Offset(centerOffset.x + outerR * cosT, centerOffset.y + outerR * sinT),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Label
                val textR = innerR - 10.dp.toPx()
                val textX = centerOffset.x + textR * cosT
                val textY = centerOffset.y + textR * sinT + (tickTextPaint.textSize / 3f)
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(label, textX, textY, tickTextPaint)
                }
            }

            // 5. Active Progress Arc (Matte Champagne Metallic Sweep)
            val activeSweep = (animatedNeedleFraction * sweepAngle).coerceAtLeast(0.5f)
            val progressBrush = Brush.sweepGradient(
                colors = listOf(
                    ChampagneGold.copy(alpha = 0.6f),
                    ChampagneLight,
                    ChampagneGold,
                    ChampagneLight
                ),
                center = centerOffset
            )

            drawArc(
                brush = progressBrush,
                startAngle = startAngle,
                sweepAngle = activeSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // =====================================================================
            // 6. Authentic 3D Speedometer Needle (เข็มเรือนไมล์ของแท้ ชัดเจน พรีเมียม)
            // =====================================================================
            val needleRad = Math.toRadians(currentNeedleAngle.toDouble())
            val cosN = cos(needleRad).toFloat()
            val sinN = sin(needleRad).toFloat()
            val perpCos = -sinN
            val perpSin = cosN

            val needleTipRadius = arcRadius - 4.dp.toPx()
            val needleBaseWidth = 4.5.dp.toPx()
            val needleTailLength = 16.dp.toPx()
            val needleTailWidth = 2.8.dp.toPx()

            // Key Coordinates for the Needle
            val tipPoint = Offset(centerOffset.x + needleTipRadius * cosN, centerOffset.y + needleTipRadius * sinN)
            val baseRight = Offset(centerOffset.x - needleBaseWidth * perpCos, centerOffset.y - needleBaseWidth * perpSin)
            val baseLeft = Offset(centerOffset.x + needleBaseWidth * perpCos, centerOffset.y + needleBaseWidth * perpSin)
            val tailTip = Offset(centerOffset.x - needleTailLength * cosN, centerOffset.y - needleTailLength * sinN)
            val tailRight = Offset(tailTip.x - needleTailWidth * perpCos, tailTip.y - needleTailWidth * perpSin)
            val tailLeft = Offset(tailTip.x + needleTailWidth * perpCos, tailTip.y + needleTailWidth * perpSin)

            // A. Drop Shadow beneath the needle for tactile physical depth
            val shadowOffset = Offset(3.dp.toPx(), 4.dp.toPx())
            val shadowPath = Path().apply {
                moveTo(tipPoint.x + shadowOffset.x, tipPoint.y + shadowOffset.y)
                lineTo(baseRight.x + shadowOffset.x, baseRight.y + shadowOffset.y)
                lineTo(tailRight.x + shadowOffset.x, tailRight.y + shadowOffset.y)
                lineTo(tailTip.x + shadowOffset.x, tailTip.y + shadowOffset.y)
                lineTo(tailLeft.x + shadowOffset.x, tailLeft.y + shadowOffset.y)
                lineTo(baseLeft.x + shadowOffset.x, baseLeft.y + shadowOffset.y)
                close()
            }
            drawPath(
                path = shadowPath,
                color = Color(0x50000000)
            )

            // B. Main Needle Blade with Sports/Aviation Instrument Gradient
            val needlePath = Path().apply {
                moveTo(tipPoint.x, tipPoint.y)
                lineTo(baseRight.x, baseRight.y)
                lineTo(tailRight.x, tailRight.y)
                lineTo(tailTip.x, tailTip.y)
                lineTo(tailLeft.x, tailLeft.y)
                lineTo(baseLeft.x, baseLeft.y)
                close()
            }

            val needleBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF8C1D40), // Darker counterweight
                    WinePink,          // Crimson body
                    Color(0xFFFF5277), // Vivid sport red
                    ChampagneGold,     // Luminous transition
                    ChampagneLight     // Brilliant needle point
                ),
                start = tailTip,
                end = tipPoint
            )
            drawPath(path = needlePath, brush = needleBrush)

            // C. Highlight Spine down the center of the needle
            drawLine(
                color = Color(0xDDFFFFFF),
                start = Offset(centerOffset.x + 10.dp.toPx() * cosN, centerOffset.y + 10.dp.toPx() * sinN),
                end = Offset(centerOffset.x + (needleTipRadius - 6.dp.toPx()) * cosN, centerOffset.y + (needleTipRadius - 6.dp.toPx()) * sinN),
                strokeWidth = 1.3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // D. Needle Tip Glow & Jewel Marker (Sweep indicator on outer track)
            drawCircle(
                color = ChampagneGold.copy(alpha = 0.45f),
                radius = 7.dp.toPx(),
                center = tipPoint
            )
            drawCircle(
                color = ChampagneLight,
                radius = 3.5.dp.toPx(),
                center = tipPoint
            )

            // E. Metallic Center Hub / Cap
            val capBrush = Brush.radialGradient(
                colors = listOf(
                    ChampagneLight,
                    ChampagneGold,
                    ChampagneDark,
                    Color(0xFF2B2011)
                ),
                center = centerOffset,
                radius = 16.dp.toPx()
            )
            drawCircle(
                brush = capBrush,
                radius = 16.dp.toPx(),
                center = centerOffset
            )
            drawCircle(
                color = if (isDark) DeepNavy else Color(0xFF1E293B),
                radius = 11.dp.toPx(),
                center = centerOffset
            )
            drawCircle(
                color = WinePink,
                radius = 4.dp.toPx(),
                center = centerOffset
            )
        }

        // =========================================================================
        // Center Digital Display & GO Action Button
        // =========================================================================
        if (phase == TestPhase.IDLE || phase == TestPhase.CANCELLED) {
            // High-Affordance Pulsing Center "GO" Button (Tap to Start)
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .scale(if (!reducedMotion) pulseScale else 1.0f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                WinePink,
                                Color(0xFF9E2A52),
                                Color(0xFF6B122E)
                            )
                        )
                    )
                    .border(2.5.dp, ChampagneGold.copy(alpha = 0.85f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White),
                        onClick = { onStartTest?.invoke() }
                    )
                    .testTag("center_go_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "GO",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "แตะเพื่อเริ่ม",
                        color = ChampagneLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // Status Pill at Top of Stack
                val (statusText, statusBorderColor, statusTextColor) = when (phase) {
                    TestPhase.IDLE -> Triple("READY", theme.colors.border, theme.colors.textMuted)
                    TestPhase.TESTING_PING -> Triple("LATENCY", ChampagneGold, ChampagneGold)
                    TestPhase.TESTING_DOWNLOAD -> Triple("DOWNLOAD", ChampagneGold, ChampagneGold)
                    TestPhase.TESTING_UPLOAD -> Triple("UPLOAD", WinePink, WinePink)
                    TestPhase.COMPLETED -> Triple("COMPLETED", StatusGreen, StatusGreen)
                    TestPhase.CANCELLED -> Triple("CANCELLED", theme.colors.border, theme.colors.textMuted)
                    TestPhase.ERROR -> Triple("ERROR", WinePink, WinePink)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBorderColor.copy(alpha = 0.12f))
                        .border(1.dp, statusBorderColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                        .testTag("gauge_phase_pill")
                ) {
                    Text(
                        text = statusText,
                        color = statusTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold, // 600
                        letterSpacing = 0.6.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Massive Speed Number in Center (Tabular Figures to prevent jitter)
                val speedDisplay = String.format(Locale.US, "%.1f", displayedSpeed)

                Text(
                    text = speedDisplay,
                    color = theme.colors.textMain,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.SemiBold, // 600
                    fontFamily = FontFamily.Monospace, // Tabular numerals
                    letterSpacing = (-1.0).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("gauge_speed_text")
                )

                // Speed Unit Clickable Pill Directly Below or Restart Button
                if (phase == TestPhase.COMPLETED) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(WinePink.copy(alpha = 0.15f))
                            .border(1.dp, WinePink.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .clickable { onStartTest?.invoke() }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("gauge_restart_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = WinePink,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "ทดสอบอีกครั้ง",
                                color = WinePink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onToggleUnit?.invoke() }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = speedUnit.label,
                            color = ChampagneGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium, // 500
                            letterSpacing = 0.4.sp,
                            modifier = Modifier.testTag("gauge_unit_text")
                        )
                    }
                }
            }
        }

        // =========================================================================
        // AdMob Completion Overlay
        // =========================================================================
        AnimatedVisibility(
            visible = showGaugeAd,
            enter = fadeIn() + scaleIn(initialScale = 0.92f),
            exit = fadeOut() + scaleOut(targetScale = 0.92f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(if (isDark) DeepNavy.copy(alpha = 0.96f) else Color.White.copy(alpha = 0.96f))
                    .border(1.dp, ChampagneGold.copy(alpha = 0.4f), CircleShape)
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(theme.colors.surface)
                                .clickable { isGaugeAdDismissed = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Ad",
                                tint = theme.colors.textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = ChampagneGold,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Zipspeed Sponsor",
                        color = theme.colors.textMain,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "High Performance Network",
                        color = theme.colors.textMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ChampagneGold.copy(alpha = 0.15f))
                            .border(1.dp, ChampagneGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { onOpenVipModal?.invoke() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Upgrade Ad-Free",
                            color = ChampagneGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

