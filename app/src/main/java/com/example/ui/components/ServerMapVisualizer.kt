package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DEFAULT_SERVERS
import com.example.model.Language
import com.example.model.ServerInfo
import com.example.ui.theme.CyberInk
import com.example.ui.theme.CyberMuted
import com.example.ui.theme.CyberPanel
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple

@Composable
fun ServerMapVisualizer(
    selectedServer: ServerInfo,
    language: Language,
    onSelectServer: (ServerInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    // User location fixed to Bangkok (13.7563° N, 100.5018° E)
    val userLat = 13.7563
    val userLng = 100.5018

    // Radar pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseRadiusFraction by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    val signalBeamPhase by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "signalBeam"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CyberPanel)
            .border(1.dp, Color(0x282F7BFF), RoundedCornerShape(20.dp))
            .padding(14.dp)
            .testTag("server_map_visualizer")
    ) {
        // Map Header Bar
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
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0x228B7CFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Map Icon",
                        tint = NeonPurple,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.str_test_server_radar_map_14),
                        color = CyberInk,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.str_network_routing_latency_visual_15),
                        color = CyberMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x2210B981))
                    .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${selectedServer.distanceKm} KM",
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cyber World Map Canvas Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF090D1A))
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()

                            // Check tap proximity to any server node
                            var closestServer: ServerInfo? = null
                            var minDistanceSq = Float.MAX_VALUE

                            DEFAULT_SERVERS.forEach { server ->
                                val (nx, ny) = projectToCanvas(
                                    server.latitude,
                                    server.longitude,
                                    userLat,
                                    userLng,
                                    width,
                                    height
                                )
                                val dx = tapOffset.x - nx
                                val dy = tapOffset.y - ny
                                val distSq = dx * dx + dy * dy
                                if (distSq < minDistanceSq && distSq < 1600f) { // 40px radius
                                    minDistanceSq = distSq
                                    closestServer = server
                                }
                            }

                            closestServer?.let { onSelectServer(it) }
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height

                // Draw Cyber Grid Lines
                val gridSpacing = 30f
                var x = 0f
                while (x < canvasW) {
                    drawLine(
                        color = Color(0x0F2F7BFF),
                        start = Offset(x, 0f),
                        end = Offset(x, canvasH),
                        strokeWidth = 1f
                    )
                    x += gridSpacing
                }
                var y = 0f
                while (y < canvasH) {
                    drawLine(
                        color = Color(0x0F2F7BFF),
                        start = Offset(0f, y),
                        end = Offset(canvasW, y),
                        strokeWidth = 1f
                    )
                    y += gridSpacing
                }

                // Project User Position (Bangkok center anchor)
                val (uX, uY) = projectToCanvas(userLat, userLng, userLat, userLng, canvasW, canvasH)

                // Concentric Distance Radar Circles
                listOf(0.2f, 0.45f, 0.75f).forEach { scale ->
                    val r = canvasW * scale * pulseRadiusFraction
                    drawCircle(
                        color = NeonBlue.copy(alpha = (1f - pulseRadiusFraction) * 0.35f),
                        radius = r,
                        center = Offset(uX, uY),
                        style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                    )
                }

                // Draw Server Pins & Laser Arcs
                DEFAULT_SERVERS.forEach { server ->
                    val (sX, sY) = projectToCanvas(
                        server.latitude,
                        server.longitude,
                        userLat,
                        userLng,
                        canvasW,
                        canvasH
                    )
                    val isSelected = server.id == selectedServer.id

                    // Arc connecting User to Server
                    if (isSelected) {
                        val path = Path().apply {
                            moveTo(uX, uY)
                            val midX = (uX + sX) / 2f
                            val midY = (uY + sY) / 2f - 30f // Curvature
                            quadraticTo(midX, midY, sX, sY)
                        }

                        // Laser Beam Arc
                        drawPath(
                            path = path,
                            color = NeonGreen.copy(alpha = 0.8f),
                            style = Stroke(
                                width = 2.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), signalBeamPhase * 20f)
                            )
                        )
                    } else {
                        // Subtle routing line for inactive servers
                        drawLine(
                            color = Color(0x22FFFFFF),
                            start = Offset(uX, uY),
                            end = Offset(sX, sY),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                    }

                    // Server Node Dot
                    val nodeColor = if (isSelected) NeonGreen else NeonBlue
                    val nodeRadius = if (isSelected) 8f else 5f

                    if (isSelected) {
                        drawCircle(
                            color = NeonGreen.copy(alpha = 0.3f),
                            radius = 16f,
                            center = Offset(sX, sY)
                        )
                    }

                    drawCircle(
                        color = nodeColor,
                        radius = nodeRadius,
                        center = Offset(sX, sY)
                    )
                }

                // Draw User Location Pinpoint ("YOU ARE HERE")
                drawCircle(
                    color = Color(0x558B7CFF),
                    radius = 14f,
                    center = Offset(uX, uY)
                )
                drawCircle(
                    color = NeonPurple,
                    radius = 7f,
                    center = Offset(uX, uY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f,
                    center = Offset(uX, uY)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected Server Routing Info Footer Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x12FFFFFF))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = NeonPurple,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = stringResource(R.string.str_bangkok_selectedserver_subloca_16),
                    color = CyberInk,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${selectedServer.basePingMs} ms • ${selectedServer.countryCode}",
                color = NeonGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// Convert geographic coordinates (latitude, longitude) to canvas pixels around user anchor
private fun projectToCanvas(
    lat: Double,
    lng: Double,
    userLat: Double,
    userLng: Double,
    canvasW: Float,
    canvasH: Float
): Pair<Float, Float> {
    // Normalized Mercator-like map projection centered on user (Bangkok)
    val scaleX = canvasW / 120f
    val scaleY = canvasH / 90f

    val dLng = (lng - userLng).toFloat()
    val dLat = (userLat - lat).toFloat() // Flip Y axis for screen space

    val cx = canvasW * 0.42f + dLng * scaleX
    val cy = canvasH * 0.52f + dLat * scaleY

    // Clamp inside canvas with margin padding
    val clampedX = cx.coerceIn(16f, canvasW - 16f)
    val clampedY = cy.coerceIn(16f, canvasH - 16f)

    return Pair(clampedX, clampedY)
}
