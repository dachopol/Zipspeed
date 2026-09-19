package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.Language
import com.example.model.SignalMapPoint
import com.example.model.SignalScannerState
import com.example.ui.theme.CyberInk
import com.example.ui.theme.CyberMuted
import com.example.ui.theme.CyberPanel
import com.example.ui.theme.GoldPro
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun D3SignalHeatmapView(
    signalState: SignalScannerState,
    language: Language,
    onToggleWalkSimulation: () -> Unit,
    onPinCurrentLocation: () -> Unit,
    onResetMap: () -> Unit,
    onRefreshScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var selectedPalette by remember { mutableStateOf("turbo") } // "turbo", "eyecare", "viridis"
    var showContours by remember { mutableStateOf(true) }
    var showPathTrail by remember { mutableStateOf(true) }
    var showNodePoints by remember { mutableStateOf(true) }
    var inspectedPointInfo by remember { mutableStateOf<String?>(null) }
    var showShareSuccess by remember { mutableStateOf(false) }

    // Convert data to JSON string for D3 consumption
    val pointsJson = remember(signalState.movementPoints, signalState.routerX, signalState.routerY) {
        val root = JSONObject()
        root.put("routerX", signalState.routerX)
        root.put("routerY", signalState.routerY)
        root.put("currentDbm", signalState.currentDbm)

        val ptsArray = JSONArray()
        signalState.movementPoints.forEachIndexed { idx, pt ->
            val pObj = JSONObject()
            pObj.put("id", pt.id)
            pObj.put("x", pt.x)
            pObj.put("y", pt.y)
            pObj.put("dbm", pt.dbm)
            pObj.put("zone", pt.zoneName)
            pObj.put("speed", pt.linkSpeedMbps)
            pObj.put("step", idx + 1)
            ptsArray.put(pObj)
        }
        root.put("points", ptsArray)
        root.toString()
    }

    // Update WebView whenever points or config changes
    LaunchedEffect(pointsJson, selectedPalette, showContours, showPathTrail, showNodePoints) {
        webViewRef?.let { wv ->
            val escapedJson = pointsJson.replace("\\", "\\\\").replace("'", "\\'")
            val script = "if (window.updateSignalData) { window.updateSignalData('$escapedJson', '$selectedPalette', $showContours, $showPathTrail, $showNodePoints); }"
            wv.evaluateJavascript(script, null)
        }
    }

    // Calculate summary statistics
    val totalPoints = signalState.movementPoints.size
    val peakPoint = signalState.movementPoints.maxByOrNull { it.dbm }
    val deadzonePoint = signalState.movementPoints.minByOrNull { it.dbm }
    val avgDbm = if (totalPoints > 0) signalState.movementPoints.map { it.dbm }.average().toInt() else signalState.currentDbm

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CyberPanel)
            .border(1.dp, Color(0x1EFFFFFF), RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("d3_heatmap_container")
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
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x225B9BF3)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = NeonBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.str_d3_signal_strength_heatmap_51),
                        color = CyberInk,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("d3_heatmap_title")
                    )
                    Text(
                        text = stringResource(R.string.str_spatial_wi_fi_coverage_map_bas_52),
                        color = CyberMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Quick live status badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (signalState.isRecordingMovement) Color(0x3334D399) else Color(0x18FFFFFF))
                    .border(
                        1.dp,
                        if (signalState.isRecordingMovement) NeonGreen.copy(alpha = 0.5f) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (signalState.isRecordingMovement) NeonGreen else CyberMuted)
                    )
                    Text(
                        text = if (signalState.isRecordingMovement)
                            (stringResource(R.string.str_scanning_53))
                        else
                            (stringResource(R.string.str_ready_54)),
                        color = if (signalState.isRecordingMovement) NeonGreen else CyberMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Heatmap Visual Stats Summary Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x0CFFFFFF))
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.str_scanned_points_55),
                    color = CyberMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = "$totalPoints จุด",
                    color = NeonBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0x1AFFFFFF)))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.str_peak_signal_56),
                    color = CyberMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = "${peakPoint?.dbm ?: signalState.currentDbm} dBm",
                    color = NeonGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0x1AFFFFFF)))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.str_deadzone_57),
                    color = CyberMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = "${deadzonePoint?.dbm ?: "-88"} dBm",
                    color = if ((deadzonePoint?.dbm ?: -88) < -80) Color(0xFFEF4444) else NeonAmber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0x1AFFFFFF)))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.str_avg_level_58),
                    color = CyberMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = "$avgDbm dBm",
                    color = GoldPro,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // D3 Webview Canvas/SVG Render Window
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F141C))
                .border(1.dp, Color(0x2E5B9BF3), RoundedCornerShape(14.dp))
                .testTag("d3_webview_viewport")
        ) {
            AndroidView(
                factory = { context ->
                    createD3HeatmapWebView(
                        context = context,
                        initialPointsJson = pointsJson,
                        initialPalette = selectedPalette,
                        showContours = showContours,
                        showPath = showPathTrail,
                        showNodes = showNodePoints,
                        onPointClicked = { pointDetails ->
                            inspectedPointInfo = pointDetails
                        }
                    ).also {
                        webViewRef = it
                    }
                },
                update = {
                    // State updates are handled reactively by LaunchedEffect to prevent UI thread freezes
                },
                modifier = Modifier.fillMaxWidth().height(300.dp)
            )

            // Overlaid Router Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xD9101622))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "AP: ${signalState.band}",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // D3 Engine Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xB3273344))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "D3.js v7 Core",
                    color = NeonBlue,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Inspected Point Info Toast/Banner
        AnimatedVisibility(visible = inspectedPointInfo != null) {
            inspectedPointInfo?.let { info ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x245B9BF3))
                        .border(1.dp, NeonBlue.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = NeonBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = info,
                            color = CyberInk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "ปิด",
                        color = CyberMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { inspectedPointInfo = null }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // D3 View & Color Layer Controls
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Palette Selectors
            listOf(
                Pair("turbo", "Turbo"),
                Pair("eyecare", "Eye-Care Soft"),
                Pair("viridis", "Viridis")
            ).forEach { (id, label) ->
                val active = selectedPalette == id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) NeonBlue else Color(0x18FFFFFF))
                        .clickable { selectedPalette = id }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = label,
                        color = if (active) Color.White else CyberMuted,
                        fontSize = 11.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // Toggle Contours
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (showContours) Color(0x33A78BFA) else Color(0x18FFFFFF))
                    .border(
                        1.dp,
                        if (showContours) NeonPurple.copy(alpha = 0.5f) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { showContours = !showContours }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (showContours) "เส้นระดับ Contours (เปิด)" else "เส้นระดับ Contours (ปิด)",
                    color = if (showContours) NeonPurple else CyberMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Toggle Path Trail
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (showPathTrail) Color(0x3334D399) else Color(0x18FFFFFF))
                    .border(
                        1.dp,
                        if (showPathTrail) NeonGreen.copy(alpha = 0.5f) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { showPathTrail = !showPathTrail }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (showPathTrail) "เส้นทางเดิน Trail (เปิด)" else "เส้นทางเดิน Trail (ปิด)",
                    color = if (showPathTrail) NeonGreen else CyberMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Movement & Scanner Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Toggle Walk Simulation
            Button(
                onClick = onToggleWalkSimulation,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (signalState.isRecordingMovement) Color(0xFFEF4444) else NeonGreen
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("btn_walk_scan_toggle")
            ) {
                Icon(
                    imageVector = if (signalState.isRecordingMovement) Icons.Default.Pause else Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = null,
                    tint = if (signalState.isRecordingMovement) Color.White else Color(0xFF071510),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (signalState.isRecordingMovement)
                        (stringResource(R.string.str_stop_walking_59))
                    else
                        (stringResource(R.string.str_walk_map_60)),
                    color = if (signalState.isRecordingMovement) Color.White else Color(0xFF071510),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Pin Location Button
            OutlinedButton(
                onClick = onPinCurrentLocation,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NeonBlue
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_pin_location")
            ) {
                Icon(
                    imageVector = Icons.Default.AddLocation,
                    contentDescription = null,
                    tint = NeonBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.str_pin_spot_61),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Reset Map Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x18FFFFFF))
                    .clickable { onResetMap() }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("btn_reset_map")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = CyberMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Heatmap Interpretation Guide
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.str_tap_point_nodes_to_inspect_sig_62),
                color = CyberMuted,
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )
        }
    }
}

/**
 * Creates the WebView embedding D3.js and the custom Canvas/SVG Heatmap engine
 */
@SuppressLint("SetJavaScriptEnabled")
private fun createD3HeatmapWebView(
    context: Context,
    initialPointsJson: String,
    initialPalette: String,
    showContours: Boolean,
    showPath: Boolean,
    showNodes: Boolean,
    onPointClicked: (String) -> Unit
): WebView {
    return WebView(context).apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            cacheMode = WebSettings.LOAD_DEFAULT
            useWideViewPort = true
            loadWithOverviewMode = true
        }
        setBackgroundColor(android.graphics.Color.TRANSPARENT)

        // Register Android JavaScript Bridge
        addJavascriptInterface(
            object {
                @JavascriptInterface
                fun onPointSelected(zone: String, dbm: Int, speed: Int, step: Int) {
                    val info = "จุดที่ #$step: $zone | $dbm dBm (${speed} Mbps)"
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        onPointClicked(info)
                    }
                }
            },
            "AndroidBridge"
        )

        webChromeClient = WebChromeClient()
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val escapedJson = initialPointsJson.replace("\\", "\\\\").replace("'", "\\'")
                val script = "if (window.updateSignalData) { window.updateSignalData('$escapedJson', '$initialPalette', $showContours, $showPath, $showNodes); }"
                view?.evaluateJavascript(script, null)
            }
        }

        loadDataWithBaseURL(
            "file:///android_asset/",
            generateD3HeatmapHtml(),
            "text/html",
            "UTF-8",
            null
        )
    }
}

/**
 * Generates the responsive HTML/JS application powered by D3.js v7
 */
private fun generateD3HeatmapHtml(): String {
    return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>D3 Signal Heatmap</title>
    <!-- Load local D3.js v7 with reliable CDN fallback -->
    <script src="d3.v7.min.js"></script>
    <script>
        if (typeof d3 === 'undefined') {
            document.write('<script src="https://cdn.jsdelivr.net/npm/d3@7"><\/script>');
        }
    </script>
    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            user-select: none;
            -webkit-user-select: none;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        }
        body, html {
            width: 100%;
            height: 100%;
            background-color: #0F141C;
            overflow: hidden;
        }
        #stage {
            position: relative;
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        canvas#heatmapCanvas {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 1;
            filter: blur(8px);
            opacity: 0.88;
        }
        svg#overlaySvg {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 2;
            pointer-events: all;
        }
        .room-wall {
            stroke: rgba(255, 255, 255, 0.22);
            stroke-width: 1.5;
            stroke-dasharray: 4, 3;
            fill: rgba(18, 26, 38, 0.45);
        }
        .room-label {
            fill: rgba(255, 255, 255, 0.55);
            font-size: 11px;
            font-weight: 600;
            letter-spacing: 0.5px;
        }
        .trajectory-path {
            fill: none;
            stroke: #5B9BF3;
            stroke-width: 2.5;
            stroke-dasharray: 6, 4;
            stroke-linecap: round;
            filter: drop-shadow(0px 0px 4px rgba(91, 155, 243, 0.7));
        }
        .contour-line {
            fill: none;
            stroke-width: 1.2;
            stroke-dasharray: 3, 3;
            opacity: 0.65;
        }
        .contour-label {
            font-size: 9px;
            font-weight: bold;
            fill: rgba(255, 255, 255, 0.75);
        }
        .point-node {
            cursor: pointer;
            transition: transform 0.2s ease;
        }
        .point-node:hover {
            transform: scale(1.3);
        }
        .router-pulse {
            animation: pulse-ring 2s cubic-bezier(0.215, 0.61, 0.355, 1) infinite;
            transform-origin: center;
        }
        @keyframes pulse-ring {
            0% { r: 6px; opacity: 0.9; }
            100% { r: 24px; opacity: 0; }
        }
        #tooltip {
            position: absolute;
            z-index: 10;
            background: rgba(15, 23, 42, 0.94);
            border: 1px solid rgba(91, 155, 243, 0.4);
            border-radius: 8px;
            padding: 6px 10px;
            color: #FFFFFF;
            font-size: 11px;
            pointer-events: none;
            display: none;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
        }
    </style>
</head>
<body>
    <div id="stage">
        <canvas id="heatmapCanvas" width="600" height="420"></canvas>
        <svg id="overlaySvg" viewBox="0 0 600 420" preserveAspectRatio="none"></svg>
        <div id="tooltip"></div>
    </div>

    <script>
        const canvas = document.getElementById('heatmapCanvas');
        const ctx = canvas.getContext('2d');
        const svg = d3.select('#overlaySvg');
        const tooltip = document.getElementById('tooltip');

        const MAP_WIDTH = 600;
        const MAP_HEIGHT = 420;

        // Room/Zone Blueprint Definitions
        const ZONES = [
            { name: "Living Room (Router)", x: 230, y: 120, w: 180, h: 160 },
            { name: "Kitchen & Dining", x: 40, y: 40, w: 170, h: 160 },
            { name: "Kitchen Balcony", x: 40, y: 220, w: 170, h: 150 },
            { name: "Working Study Room", x: 430, y: 40, w: 130, h: 170 },
            { name: "Master Bedroom", x: 430, y: 230, w: 130, h: 140 }
        ];

        let currentData = { routerX: 50, routerY: 45, points: [] };
        let currentPalette = 'turbo';
        let showContoursOption = true;
        let showPathOption = true;
        let showNodesOption = true;

        // D3 Color Scales
        function getColorScale(paletteName) {
            if (paletteName === 'eyecare') {
                // Soft Eye-Care Mint / Sky / Amber / Crimson
                return d3.scaleLinear()
                    .domain([-95, -82, -72, -60, -45, -35])
                    .range(['#4C1D95', '#9333EA', '#D97706', '#34D399', '#10B981', '#059669'])
                    .clamp(true);
            } else if (paletteName === 'viridis') {
                return d3.scaleSequential()
                    .domain([-95, -35])
                    .interpolator(d3.interpolateViridis);
            } else {
                // Turbo / Thermal spectrum
                return d3.scaleLinear()
                    .domain([-95, -85, -75, -65, -52, -35])
                    .range(['#7F1D1D', '#EF4444', '#F59E0B', '#10B981', '#059669', '#38BDF8'])
                    .clamp(true);
            }
        }

        // Global function called from Android Kotlin via evaluateJavascript
        window.updateSignalData = function(jsonStr, palette, contours, path, nodes) {
            try {
                currentData = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr;
                currentPalette = palette || 'turbo';
                showContoursOption = contours !== undefined ? contours : true;
                showPathOption = path !== undefined ? path : true;
                showNodesOption = nodes !== undefined ? nodes : true;
                renderAll();
            } catch(e) {
                console.error("Error updating signal data:", e);
            }
        };

        function renderAll() {
            renderHeatmapCanvas();
            renderSvgOverlay();
        }

        // Render continuous IDW (Inverse Distance Weighting) heatmap on Canvas (optimized for high FPS)
        function renderHeatmapCanvas() {
            ctx.clearRect(0, 0, MAP_WIDTH, MAP_HEIGHT);
            const points = currentData.points || [];
            if (points.length === 0) return;

            const colorScale = getColorScale(currentPalette);

            const gridStep = 16; // Optimized pixel resolution (fast and smooth with CSS blur)
            const cols = Math.ceil(MAP_WIDTH / gridStep);
            const rows = Math.ceil(MAP_HEIGHT / gridStep);

            // Compute IDW value for each grid cell using fast squared distances
            for (let r = 0; r < rows; r++) {
                const py = r * gridStep;
                for (let c = 0; c < cols; c++) {
                    const px = c * gridStep;

                    let sumW = 0;
                    let sumVal = 0;

                    for (let i = 0; i < points.length; i++) {
                        const pt = points[i];
                        const x = (pt.x / 100) * MAP_WIDTH;
                        const y = (pt.y / 100) * MAP_HEIGHT;

                        const dx = px - x;
                        const dy = py - y;
                        const distSq = dx * dx + dy * dy;

                        // Fast Inverse Distance Squared with smoothing offset
                        const w = 1 / (distSq + 576);
                        sumW += w;
                        sumVal += w * pt.dbm;
                    }

                    const dbmVal = sumW > 0 ? (sumVal / sumW) : -85;
                    ctx.fillStyle = colorScale(dbmVal);
                    ctx.fillRect(px, py, gridStep, gridStep);
                }
            }
        }

        // Render Floorplan, Contours, Trajectory Path, Router & Markers on D3 SVG
        function renderSvgOverlay() {
            svg.selectAll('*').remove();

            const points = currentData.points || [];
            const colorScale = getColorScale(currentPalette);

            // 1. Draw Architectural Floorplan Rooms
            const roomsGroup = svg.append('g').attr('class', 'rooms-layer');
            ZONES.forEach(z => {
                roomsGroup.append('rect')
                    .attr('class', 'room-wall')
                    .attr('x', z.x)
                    .attr('y', z.y)
                    .attr('width', z.w)
                    .attr('height', z.h)
                    .attr('rx', 6);

                roomsGroup.append('text')
                    .attr('class', 'room-label')
                    .attr('x', z.x + 8)
                    .attr('y', z.y + 18)
                    .text(z.name);
            });

            // 2. Draw D3 Equipotential Contour Rings
            if (showContoursOption && points.length > 0) {
                const contourGroup = svg.append('g').attr('class', 'contours-layer');
                const rx = (currentData.routerX / 100) * MAP_WIDTH;
                const ry = (currentData.routerY / 100) * MAP_HEIGHT;

                const levels = [
                    { r: 45, dbm: -50, color: '#10B981', label: "-50 dBm (Strong)" },
                    { r: 95, dbm: -65, color: '#34D399', label: "-65 dBm (Good)" },
                    { r: 155, dbm: -75, color: '#F59E0B', label: "-75 dBm (Fair)" },
                    { r: 220, dbm: -85, color: '#EF4444', label: "-85 dBm (Weak)" }
                ];

                levels.forEach(lvl => {
                    contourGroup.append('ellipse')
                        .attr('class', 'contour-line')
                        .attr('cx', rx)
                        .attr('cy', ry)
                        .attr('rx', lvl.r * 1.15)
                        .attr('ry', lvl.r * 0.95)
                        .attr('stroke', lvl.color);

                    contourGroup.append('text')
                        .attr('class', 'contour-label')
                        .attr('x', rx + lvl.r * 1.05)
                        .attr('y', ry - 6)
                        .text(lvl.label);
                });
            }

            // 3. Draw User Movement Trajectory Path using D3.line
            if (showPathOption && points.length > 1) {
                const lineGenerator = d3.line()
                    .x(d => (d.x / 100) * MAP_WIDTH)
                    .y(d => (d.y / 100) * MAP_HEIGHT)
                    .curve(d3.curveCatmullRom.alpha(0.5));

                svg.append('path')
                    .datum(points)
                    .attr('class', 'trajectory-path')
                    .attr('d', lineGenerator);
            }

            // 4. Draw Router / AP Location Marker
            const rX = (currentData.routerX / 100) * MAP_WIDTH;
            const rY = (currentData.routerY / 100) * MAP_HEIGHT;

            const routerG = svg.append('g').attr('class', 'router-group');
            // Pulsing rings
            routerG.append('circle')
                .attr('cx', rX)
                .attr('cy', rY)
                .attr('r', 8)
                .attr('fill', 'none')
                .attr('stroke', '#34D399')
                .attr('stroke-width', 2)
                .attr('class', 'router-pulse');

            routerG.append('circle')
                .attr('cx', rX)
                .attr('cy', rY)
                .attr('r', 8)
                .attr('fill', '#10B981')
                .attr('stroke', '#FFFFFF')
                .attr('stroke-width', 2);

            routerG.append('text')
                .attr('x', rX + 12)
                .attr('y', rY + 4)
                .attr('fill', '#34D399')
                .attr('font-size', '10px')
                .attr('font-weight', 'bold')
                .text("Wi-Fi 6 Router");

            // 5. Draw Interactive Scanned Nodes
            if (showNodesOption && points.length > 0) {
                const nodesGroup = svg.append('g').attr('class', 'nodes-layer');

                points.forEach((pt, idx) => {
                    const px = (pt.x / 100) * MAP_WIDTH;
                    const py = (pt.y / 100) * MAP_HEIGHT;
                    const isLast = idx === points.length - 1;

                    const nodeG = nodesGroup.append('g')
                        .attr('class', 'point-node')
                        .on('click', function(event) {
                            showTooltip(event, pt, px, py);
                            if (window.AndroidBridge && window.AndroidBridge.onPointSelected) {
                                window.AndroidBridge.onPointSelected(pt.zone, pt.dbm, pt.speed || 866, pt.step || (idx + 1));
                            }
                        });

                    // Outer halo for latest point
                    if (isLast) {
                        nodeG.append('circle')
                            .attr('cx', px)
                            .attr('cy', py)
                            .attr('r', 12)
                            .attr('fill', 'none')
                            .attr('stroke', '#5B9BF3')
                            .attr('stroke-width', 2)
                            .attr('opacity', 0.85);
                    }

                    // Solid node circle
                    nodeG.append('circle')
                        .attr('cx', px)
                        .attr('cy', py)
                        .attr('r', isLast ? 7 : 5.5)
                        .attr('fill', colorScale(pt.dbm))
                        .attr('stroke', '#FFFFFF')
                        .attr('stroke-width', 1.5);

                    // Step Index number
                    nodeG.append('text')
                        .attr('x', px)
                        .attr('y', py - 8)
                        .attr('text-anchor', 'middle')
                        .attr('fill', '#FFFFFF')
                        .attr('font-size', '8.5px')
                        .attr('font-weight', 'bold')
                        .text('#' + (pt.step || (idx + 1)));
                });
            }

            // 6. Draw D3 Heatmap Color Legend Bar at Bottom
            renderLegend(colorScale);
        }

        // Render D3 Color Legend Bar
        function renderLegend(colorScale) {
            const legendW = 200;
            const legendH = 8;
            const lx = MAP_WIDTH - legendW - 14;
            const ly = MAP_HEIGHT - 22;

            const legendG = svg.append('g').attr('class', 'legend-group');

            // Defs gradient
            const defs = svg.append('defs');
            const gradId = 'heatmap-legend-grad';
            const grad = defs.append('linearGradient')
                .attr('id', gradId)
                .attr('x1', '0%').attr('y1', '0%')
                .attr('x2', '100%').attr('y2', '0%');

            const stops = [-95, -80, -70, -60, -45, -35];
            stops.forEach((val, i) => {
                const offset = ((val - (-95)) / ((-35) - (-95))) * 100;
                grad.append('stop')
                    .attr('offset', offset + '%')
                    .attr('stop-color', colorScale(val));
            });

            // Gradient bar
            legendG.append('rect')
                .attr('x', lx)
                .attr('y', ly)
                .attr('width', legendW)
                .attr('height', legendH)
                .attr('rx', 4)
                .attr('fill', 'url(#' + gradId + ')')
                .attr('stroke', 'rgba(255,255,255,0.25)')
                .attr('stroke-width', 1);

            // Labels
            legendG.append('text')
                .attr('x', lx)
                .attr('y', ly - 4)
                .attr('fill', '#EF4444')
                .attr('font-size', '8.5px')
                .attr('font-weight', 'bold')
                .text('-95 dBm (อับ)');

            legendG.append('text')
                .attr('x', lx + legendW / 2)
                .attr('y', ly - 4)
                .attr('text-anchor', 'middle')
                .attr('fill', '#F59E0B')
                .attr('font-size', '8.5px')
                .attr('font-weight', 'bold')
                .text('-70 dBm');

            legendG.append('text')
                .attr('x', lx + legendW)
                .attr('y', ly - 4)
                .attr('text-anchor', 'end')
                .attr('fill', '#34D399')
                .attr('font-size', '8.5px')
                .attr('font-weight', 'bold')
                .text('-40 dBm (แรง)');
        }

        // Show HTML Tooltip
        function showTooltip(event, pt, px, py) {
            tooltip.style.display = 'block';
            tooltip.style.left = Math.min(px + 10, MAP_WIDTH - 140) + 'px';
            tooltip.style.top = Math.max(py - 40, 10) + 'px';
            tooltip.innerHTML = '<strong>' + (pt.zone || 'ตำแหน่งสแกน') + '</strong><br/>' +
                                pt.dbm + ' dBm &bull; ' + (pt.speed || 866) + ' Mbps';

            setTimeout(() => {
                tooltip.style.display = 'none';
            }, 3500);
        }

        // Initial trigger
        renderAll();
    </script>
</body>
</html>
    """.trimIndent()
}
