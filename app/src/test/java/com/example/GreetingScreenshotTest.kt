package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.engine.NetworkIpInfo
import com.example.model.DEFAULT_SERVERS
import com.example.model.Language
import com.example.model.SpeedTestState
import com.example.model.SpeedUnit
import com.example.model.TestPhase
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberBgGradEnd
import com.example.ui.theme.ZipspeedTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val realisticTestState = SpeedTestState(
      phase = TestPhase.TESTING_DOWNLOAD,
      progressFraction = 0.68f,
      liveSpeed = 485.6,
      pingMs = 12,
      downloadMbps = 485.6,
      uploadMbps = null,
      jitterMs = 2,
      packetLossPercent = 0.0
    )

    val realisticServer = DEFAULT_SERVERS.first()
    val realisticIpInfo = NetworkIpInfo(
      publicIp = "171.96.124.89",
      localIp = "192.168.1.108",
      ispName = "AIS Fibre Broadband (Bangkok)",
      countryCode = "TH",
      isFetching = false
    )

    composeTestRule.setContent {
      ZipspeedTheme {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.radialGradient(
                colors = listOf(
                  Color(0x282F7BFF),
                  Color(0x188B7CFF),
                  CyberBg
                ),
                center = androidx.compose.ui.geometry.Offset(500f, 300f),
                radius = 900f
              )
            )
            .background(CyberBgGradEnd.copy(alpha = 0.85f))
        ) {
          HomeScreen(
            testState = realisticTestState,
            selectedServer = realisticServer,
            ipInfo = realisticIpInfo,
            speedUnit = SpeedUnit.MBPS,
            language = Language.EN,
            isProPlan = true,
            reducedMotion = true,
            isPrecisionMode = false,
            onStartTest = {},
            onStartPrecisionTest = {},
            onCancelTest = {},
            onOpenServerModal = {},
            onRefreshIp = {},
            onOpenAdModal = {}
          )
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

