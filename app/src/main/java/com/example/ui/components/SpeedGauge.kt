package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
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
import com.example.ui.theme.CyberInk
import com.example.ui.theme.CyberMuted
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import java.util.Locale
import kotlin.math.max

/**
 * Maps real-time network speed (in Mbps) to a normalized dial fraction [0f..1f].
 * Uses a progressive curve so lower bandwidth (1-50 Mbps) and gigabit speeds (100-1000 Mbps)
 * are visually prominent and clear on the speedometer scale.
 */
private fun calculateSpeedFraction(speed: Double): Float {
    if (speed <= 0.001) return 0f
    return when {
        speed < 10.0 -> (speed / 10.0).toFloat() * 0.15f
        speed < 50.0 -> 0.15f + ((speed - 10.0) / 40.0).toFloat() * 0.25f
        speed < 100.0 -> 0.40f + ((speed - 50.0) / 50.0).toFloat() * 0.20f
        speed < 300.0 -> 0.60f + ((speed - 100.0) / 200.0).toFloat() * 0.20f
        speed < 1000.0 -> 0.80f + ((speed - 300.0) / 700.0).toFloat() * 0.20f
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
    onToggleUnit: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

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

    val triggerHaptic = remember(vibrator, hapticFeedback) {
        { intensity: Int ->
            try {
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val effect = when (intensity) {
                            2 -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                            3 -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                            else -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                        }
                        vibrator.vibrate(effect)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val duration = when (intensity) {
                            2 -> 25L
                            3 -> 50L
                            else -> 12L
                        }
                        val amplitude = when (intensity) {
                            2 -> 180
                            3 -> 255
                            else -> 90
                        }
                        vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(15L)
                    }
                } else {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            } catch (_: Throwable) {
                // Safely ignored in environments without haptic motor
            }
        }
    }

    // Tactile haptic feedback on speed test phase transitions
    LaunchedEffect(phase) {
        when (phase) {
            TestPhase.TESTING_PING -> triggerHaptic(1)
            TestPhase.TESTING_DOWNLOAD, TestPhase.TESTING_UPLOAD -> triggerHaptic(2)
            TestPhase.COMPLETED -> triggerHaptic(3)
            TestPhase.ERROR, TestPhase.IDLE -> {}
        }
    }

    val targetNeedleFraction = calculateSpeedFraction(speedValue)

    // Dynamic mechanical tick vibration as needle revs across dial notches during active testing
    val lastNotch = remember { mutableIntStateOf(0) }
    val lastTickTime = remember { mutableLongStateOf(0L) }

    LaunchedEffect(targetNeedleFraction, isTesting) {
        if (isTesting && targetNeedleFraction > 0.02f) {
            val currentNotch = (targetNeedleFraction * 24).toInt()
            val now = System.currentTimeMillis()
            if (currentNotch != lastNotch.intValue && now - lastTickTime.longValue >= 60L) {
                lastNotch.intValue = currentNotch
                lastTickTime.longValue = now
                triggerHaptic(1)
            }
        } else if (!isTesting) {
            lastNotch.intValue = 0
        }
    }

    // Spring-based physics animation for the gauge needle with natural mechanical damping and responsiveness
    val needleSpringSpec: AnimationSpec<Float> = remember(isTesting, reducedMotion) {
        if (reducedMotion) {
            tween(durationMillis = 100, easing = LinearEasing)
        } else {
            spring(
                dampingRatio = if (isTesting) 0.64f else 0.78f, // Natural underdamping for organic recoil during speed jumps
                stiffness = if (isTesting) 360f else 280f       // Fast physical response tracking live bandwidth fluctuations
            )
        }
    }

    val animatedNeedleFraction by animateFloatAsState(
        targetValue = targetNeedleFraction,
        animationSpec = needleSpringSpec,
        label = "animatedNeedleFraction"
    )

    val animatedSpeed by animateFloatAsState(
        targetValue = speedValue.toFloat(),
        animationSpec = if (reducedMotion) {
            tween(durationMillis = 100, easing = LinearEasing)
        } else {
            spring(
                dampingRatio = if (isTesting) 0.68f else 0.80f,
                stiffness = if (isTesting) 360f else 280f
            )
        },
        label = "animatedSpeed"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "animatedProgress"
    )

    // Calculate dynamic color based on speed magnitude: Blue (low) -> Purple (mid) -> Green (high)
    val speedRatio = (animatedSpeed / 120f).coerceIn(0f, 1f)
    val targetSpeedColor = when {
        speedRatio < 0.5f -> lerp(NeonBlue, NeonPurple, speedRatio * 2f)
        else -> lerp(NeonPurple, NeonGreen, (speedRatio - 0.5f) * 2f)
    }

    val animatedGaugeColor by animateColorAsState(
        targetValue = targetSpeedColor,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "animatedGaugeColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "gaugeOrbit")
    val orbitRotation by if (reducedMotion) {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotateRing"
        )
    }

    val drawRingFraction by if (reducedMotion) {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0.85f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "drawRing"
        )
    }

    val sparkPulse by if (reducedMotion) {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "spark"
        )
    }

    // Micro-vibration flutter simulating organic analog needle response to real-time packet stream fluctuations
    val needleFlutter by if (reducedMotion || !isTesting || speedValue <= 0.5) {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = -0.003f,
            targetValue = 0.003f,
            animationSpec = infiniteRepeatable(
                animation = tween(80, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "needleFlutter"
        )
    }

    // Convert speed if unit is MB/s (1 Byte = 8 bits)
    val displaySpeed = if (speedUnit == SpeedUnit.MB_S) animatedSpeed / 8.0 else animatedSpeed.toDouble()
    val formattedSpeedStr = String.format(Locale.US, "%.1f", displaySpeed)

    Box(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .aspectRatio(1.0f),
        contentAlignment = Alignment.Center
    ) {
        // Reactive Ambient Glow Backdrop shifting color with speed
        Box(
            modifier = Modifier
                .fillMaxSize(0.75f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            animatedGaugeColor.copy(alpha = 0.28f),
                            animatedGaugeColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Outer Orbit Speed Ring with #34D399 Drop-Shadow Glow (.speed-ring CSS effect)
        Canvas(
            modifier = Modifier
                .fillMaxSize(0.96f)
                .rotate(orbitRotation)
        ) {
            val strokeWidth = 3.dp.toPx()
            val glowColor = Color(0xFF34D399)

            // Drop-shadow outer aura glow (15px halo)
            drawArc(
                color = glowColor.copy(alpha = 0.25f),
                startAngle = 0f,
                sweepAngle = 360f * drawRingFraction,
                useCenter = false,
                style = Stroke(width = strokeWidth * 3.5f, cap = StrokeCap.Round)
            )

            // Main Glowing Ring Arc (85% of ring circle = 306deg)
            drawArc(
                color = glowColor,
                startAngle = 0f,
                sweepAngle = 360f * drawRingFraction,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Dashed accent track
            drawCircle(
                color = animatedGaugeColor.copy(alpha = 0.3f),
                style = Stroke(
                    width = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 16f), 0f)
                )
            )
        }

        // Ambient Decorative Sparks
        Box(
            modifier = Modifier
                .fillMaxSize(0.96f)
        ) {
            // Spark Top-Right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 40.dp)
                    .size(6.dp)
                    .scale(sparkPulse)
                    .clip(CircleShape)
                    .background(animatedGaugeColor)
            )
            // Spark Bottom-Left
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 36.dp, start = 36.dp)
                    .size(6.dp)
                    .scale(sparkPulse)
                    .clip(CircleShape)
                    .background(NeonPurple)
            )
            // Spark Mid-Left
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .size(5.dp)
                    .scale(sparkPulse)
                    .clip(CircleShape)
                    .background(NeonGreen)
            )
        }

        // Main Speedometer Arc and Needle Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .testTag("speed_gauge_canvas")
        ) {
            val strokeWidth = 14.dp.toPx()
            val startAngle = 135f
            val sweepAngle = 270f
            val arcRadius = (size.minDimension - strokeWidth) / 2f
            val centerOffset = this.center

            // 1. Background Track
            drawArc(
                color = Color(0x14FFFFFF),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Current needle angle based on real-time spring physics animated fraction with organic packet flutter
            val effectiveNeedleFraction = (animatedNeedleFraction + needleFlutter).coerceIn(0f, 1f)
            val currentNeedleAngle = startAngle + (effectiveNeedleFraction * sweepAngle)

            // 2. Speed Scale Tick Marks along the arc
            val totalTicks = 28
            for (i in 0..totalTicks) {
                val fraction = i.toFloat() / totalTicks
                val tickAngle = startAngle + (fraction * sweepAngle)
                val tickRad = Math.toRadians(tickAngle.toDouble())
                val cosT = Math.cos(tickRad).toFloat()
                val sinT = Math.sin(tickRad).toFloat()

                val isMajor = i % 4 == 0
                val tickLength = if (isMajor) 7.dp.toPx() else 3.5.dp.toPx()
                val tickWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()

                val outerR = arcRadius - (strokeWidth / 2f) - 3.dp.toPx()
                val innerR = outerR - tickLength

                val isPassed = tickAngle <= currentNeedleAngle + 0.8f
                val tickColor = if (isPassed) {
                    animatedGaugeColor.copy(alpha = if (isMajor) 0.95f else 0.65f)
                } else {
                    Color(0x2AFFFFFF)
                }

                drawLine(
                    color = tickColor,
                    start = Offset(centerOffset.x + innerR * cosT, centerOffset.y + innerR * sinT),
                    end = Offset(centerOffset.x + outerR * cosT, centerOffset.y + outerR * sinT),
                    strokeWidth = tickWidth,
                    cap = StrokeCap.Round
                )
            }

            // 3. Dynamic Progress Arc with Speed-Responsive Color Shift
            val activeSweep = max(
                animatedNeedleFraction * sweepAngle,
                if (animatedProgress > 0.001f && speedValue <= 0.001) animatedProgress * 0.12f * sweepAngle else 0f
            )

            if (activeSweep > 0.5f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            NeonBlue,
                            animatedGaugeColor,
                            lerp(animatedGaugeColor, NeonGreen, 0.5f),
                            animatedGaugeColor
                        )
                    ),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 4. Speed Test Gauge Needle with Smooth Spring Transition
            val needleRad = Math.toRadians(currentNeedleAngle.toDouble())
            val cosA = Math.cos(needleRad).toFloat()
            val sinA = Math.sin(needleRad).toFloat()
            val perpCos = -sinA
            val perpSin = cosA

            val needleBaseRadius = arcRadius * 0.54f
            val needleShoulderRadius = arcRadius * 0.78f
            val needleTipRadius = arcRadius + (strokeWidth * 0.05f)

            val baseHalfWidth = 3.5.dp.toPx()
            val shoulderHalfWidth = 1.8.dp.toPx()

            val baseLeft = Offset(
                centerOffset.x + needleBaseRadius * cosA + baseHalfWidth * perpCos,
                centerOffset.y + needleBaseRadius * sinA + baseHalfWidth * perpSin
            )
            val baseRight = Offset(
                centerOffset.x + needleBaseRadius * cosA - baseHalfWidth * perpCos,
                centerOffset.y + needleBaseRadius * sinA - baseHalfWidth * perpSin
            )
            val shoulderLeft = Offset(
                centerOffset.x + needleShoulderRadius * cosA + shoulderHalfWidth * perpCos,
                centerOffset.y + needleShoulderRadius * sinA + shoulderHalfWidth * perpSin
            )
            val shoulderRight = Offset(
                centerOffset.x + needleShoulderRadius * cosA - shoulderHalfWidth * perpCos,
                centerOffset.y + needleShoulderRadius * sinA - shoulderHalfWidth * perpSin
            )
            val tip = Offset(
                centerOffset.x + needleTipRadius * cosA,
                centerOffset.y + needleTipRadius * sinA
            )

            val needlePath = Path().apply {
                moveTo(baseLeft.x, baseLeft.y)
                lineTo(shoulderLeft.x, shoulderLeft.y)
                lineTo(tip.x, tip.y)
                lineTo(shoulderRight.x, shoulderRight.y)
                lineTo(baseRight.x, baseRight.y)
                close()
            }

            // Needle subtle drop shadow
            val shadowOffset = 2.dp.toPx()
            val shadowPath = Path().apply {
                moveTo(baseLeft.x + shadowOffset, baseLeft.y + shadowOffset)
                lineTo(shoulderLeft.x + shadowOffset, shoulderLeft.y + shadowOffset)
                lineTo(tip.x + shadowOffset, tip.y + shadowOffset)
                lineTo(shoulderRight.x + shadowOffset, shoulderRight.y + shadowOffset)
                lineTo(baseRight.x + shadowOffset, baseRight.y + shadowOffset)
                close()
            }
            drawPath(shadowPath, color = Color(0x3D000000))

            // Needle body with cyber neon gradient
            drawPath(
                path = needlePath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        animatedGaugeColor.copy(alpha = 0.5f),
                        animatedGaugeColor,
                        Color.White
                    ),
                    start = Offset(centerOffset.x + needleBaseRadius * cosA, centerOffset.y + needleBaseRadius * sinA),
                    end = tip
                )
            )

            // Needle center base anchor accent
            drawCircle(
                color = animatedGaugeColor,
                radius = 3.5.dp.toPx(),
                center = Offset(centerOffset.x + needleBaseRadius * cosA, centerOffset.y + needleBaseRadius * sinA)
            )

            // Needle Tip Radiant Beacon / Glowing Pointer Bead
            drawCircle(
                color = animatedGaugeColor.copy(alpha = 0.40f),
                radius = 7.dp.toPx(),
                center = tip
            )
            drawCircle(
                color = Color.White,
                radius = 2.8.dp.toPx(),
                center = tip
            )
        }

        // Inner Circular Glass Plate with Speed Color Highlight
        Box(
            modifier = Modifier
                .fillMaxSize(0.58f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            animatedGaugeColor.copy(alpha = 0.15f),
                            Color(0x0AFFFFFF),
                            Color(0x1A000000)
                        )
                    )
                )
                .border(1.2.dp, animatedGaugeColor.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formattedSpeedStr,
                    color = CyberInk,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1.5).sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("speed_value_text")
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(animatedGaugeColor.copy(alpha = 0.12f))
                        .border(1.dp, animatedGaugeColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .clickable(enabled = onToggleUnit != null && !isTesting) {
                            triggerHaptic(1)
                            onToggleUnit?.invoke()
                        }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .testTag("speed_unit_toggle")
                ) {
                    Text(
                        text = speedUnit.label,
                        color = animatedGaugeColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.testTag("speed_unit_text")
                    )
                }
            }
        }
    }
}
