package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppScreen
import com.example.model.NavTab
import com.example.ui.ZipspeedViewModel
import com.example.ui.components.AdRewardModal
import com.example.ui.components.BottomNavBar
import com.example.ui.components.ServerSelectionModal
import com.example.ui.components.TopHeader
import com.example.ui.components.UpgradeModal
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SpeedLimitScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberBgGradEnd
import com.example.ui.theme.ZipspeedTheme
import com.example.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {

    private val viewModel: ZipspeedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (intent?.getBooleanExtra("navigate_to_diagnostics", false) == true) {
            viewModel.selectTab(NavTab.DIAGNOSTICS)
        }

        setContent {
            val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
            ZipspeedTheme(theme = currentTheme) {
                ZipspeedMainApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("navigate_to_diagnostics", false)) {
            viewModel.selectTab(NavTab.DIAGNOSTICS)
        }
    }
}

@Composable
fun ZipspeedMainApp(viewModel: ZipspeedViewModel) {
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isProPlan by viewModel.isProPlan.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val reducedMotion by viewModel.reducedMotion.collectAsStateWithLifecycle()
    val batterySaver by viewModel.batterySaver.collectAsStateWithLifecycle()
    val autoSaveHistory by viewModel.autoSaveHistory.collectAsStateWithLifecycle()
    val speedUnit by viewModel.speedUnit.collectAsStateWithLifecycle()
    val isPrecisionMode by viewModel.isPrecisionMode.collectAsStateWithLifecycle()
    val testState by viewModel.testState.collectAsStateWithLifecycle()
    val historyRecords by viewModel.historyRecords.collectAsStateWithLifecycle()
    val ipInfo by viewModel.ipInfo.collectAsStateWithLifecycle()
    val usedTestCount by viewModel.usedTestCount.collectAsStateWithLifecycle()
    val isLimitReached = !isProPlan && usedTestCount >= viewModel.maxTestLimit

    val videoTestState by viewModel.videoTestState.collectAsStateWithLifecycle()
    val webTestState by viewModel.webTestState.collectAsStateWithLifecycle()
    val mobileState by viewModel.mobileState.collectAsStateWithLifecycle()
    val downdetectorServices by viewModel.downdetectorServices.collectAsStateWithLifecycle()
    val selectedRegion by viewModel.selectedRegion.collectAsStateWithLifecycle()
    val ispBenchmarks by viewModel.ispBenchmarks.collectAsStateWithLifecycle()
    val signalScannerState by viewModel.signalScannerState.collectAsStateWithLifecycle()
    val rewardAdsEnabled by viewModel.rewardAdsEnabled.collectAsStateWithLifecycle()
    val personalizedAdsEnabled by viewModel.personalizedAdsEnabled.collectAsStateWithLifecycle()

    val showUpgradeModal by viewModel.showUpgradeModal.collectAsStateWithLifecycle()
    val showServerModal by viewModel.showServerModal.collectAsStateWithLifecycle()
    val showAdModal by viewModel.showAdModal.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        currentTheme.colors.primary.copy(alpha = 0.22f),
                        currentTheme.colors.accent.copy(alpha = 0.10f),
                        currentTheme.colors.background
                    ),
                    center = androidx.compose.ui.geometry.Offset(500f, 300f),
                    radius = 900f
                )
            )
            .background(currentTheme.colors.background.copy(alpha = 0.90f))
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screenTransition",
            modifier = Modifier.fillMaxSize()
        ) { screen ->
            when (screen) {
                AppScreen.SPLASH -> {
                    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                        SplashScreen(
                            language = language,
                            onLanguageChange = { viewModel.setLanguage(it) },
                            onStart = { viewModel.startApp() }
                        )
                    }
                }

                AppScreen.LOGIN -> {
                    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                        LoginScreen(
                            language = language,
                            onLoginSuccess = { viewModel.loginUser(it) },
                            onSkipGuest = { viewModel.skipLogin() }
                        )
                    }
                }

                AppScreen.MAIN -> {
                    Scaffold(
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets.statusBars,
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            BottomNavBar(
                                activeTab = activeTab,
                                language = language,
                                onTabSelected = { viewModel.selectTab(it) }
                            )
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // Header Bar
                            TopHeader(
                                language = language,
                                isProPlan = isProPlan,
                                reducedMotion = reducedMotion,
                                onLanguageChange = { viewModel.setLanguage(it) },
                                onOpenUpgradeModal = { viewModel.openUpgradeModal() }
                            )

                            // Tab Content View
                            AnimatedContent(
                                targetState = activeTab,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "tabTransition",
                                modifier = Modifier.weight(1f)
                            ) { targetTab ->
                                when (targetTab) {
                                    NavTab.HOME -> {
                                        HomeScreen(
                                            testState = testState,
                                            selectedServer = selectedServer,
                                            ipInfo = ipInfo,
                                            speedUnit = speedUnit,
                                            language = language,
                                            isProPlan = isProPlan,
                                            reducedMotion = reducedMotion || batterySaver,
                                            isPrecisionMode = isPrecisionMode,
                                            isLimitReached = isLimitReached,
                                            videoState = videoTestState,
                                            webState = webTestState,
                                            mobileState = mobileState,
                                            onStartTest = { viewModel.startSpeedTest() },
                                            onStartPrecisionTest = { viewModel.startPrecisionSpeedTest() },
                                            onCancelTest = { viewModel.cancelSpeedTest() },
                                            onStartVideoTest = { viewModel.startVideoTest() },
                                            onCancelVideoTest = { viewModel.cancelVideoTest() },
                                            onStartWebTest = { viewModel.startWebTest() },
                                            onRefreshMobileScan = { viewModel.refreshMobileScan() },
                                            onOpenServerModal = { viewModel.openServerModal() },
                                            onRefreshIp = { viewModel.refreshIpInfo() },
                                            onOpenAdModal = { viewModel.openAdModal() },
                                            onOpenUpgradeModal = { viewModel.openUpgradeModal() },
                                            onToggleSpeedUnit = { viewModel.toggleSpeedUnit() }
                                        )
                                    }

                                    NavTab.DIAGNOSTICS -> {
                                        DiagnosticsScreen(
                                            language = language,
                                            downdetectorServices = downdetectorServices,
                                            ispBenchmarks = ispBenchmarks,
                                            signalState = signalScannerState,
                                            selectedRegion = selectedRegion,
                                            onSelectRegion = { viewModel.selectRegion(it) },
                                            onReportOutage = { viewModel.reportOutage(it) },
                                            onToggleBackgroundScan = { viewModel.toggleBackgroundScan() },
                                            onRefreshScan = { viewModel.refreshSignalScan() },
                                            onToggleWalkSimulation = { viewModel.toggleWalkSimulation() },
                                            onPinCurrentLocation = { viewModel.pinCurrentLocation() },
                                            onResetMovementMap = { viewModel.resetMovementMap() },
                                            onToggleAlertNotifications = { viewModel.toggleSignalAlertNotifications() },
                                            onSetAlertThreshold = { viewModel.setSignalAlertThreshold(it) },
                                            onSendTestAlert = { viewModel.sendTestSignalAlert() },
                                            onDismissAlert = { viewModel.dismissCurrentAlert() },
                                            onToggleScannerScheduleEnabled = { viewModel.toggleScannerScheduleEnabled() },
                                            onSetSchedulePreset = { viewModel.setSchedulePreset(it) },
                                            onSetCustomScheduleHours = { start, end -> viewModel.setCustomScheduleHours(start, end) },
                                            onSetMinBatteryThreshold = { viewModel.setMinBatteryThreshold(it) },
                                            onToggleBypassBatteryWhenCharging = { viewModel.toggleBypassBatteryWhenCharging() },
                                            onSetSimulatedBatteryLevel = { viewModel.setSimulatedBatteryLevel(it) },
                                            onToggleSimulatedCharging = { viewModel.toggleSimulatedCharging() },
                                            onRefreshBatteryStatus = { viewModel.refreshBatteryStatus() }
                                        )
                                    }

                                    NavTab.LIMIT -> {
                                        SpeedLimitScreen(
                                            usedCount = usedTestCount,
                                            maxLimit = viewModel.maxTestLimit,
                                            isProPlan = isProPlan,
                                            language = language,
                                            reducedMotion = reducedMotion || batterySaver,
                                            onNavigateToHome = { viewModel.selectTab(NavTab.HOME) },
                                            onOpenUpgradeModal = { viewModel.openUpgradeModal() },
                                            onResetLimit = { viewModel.resetLimit() }
                                        )
                                    }

                                    NavTab.HISTORY -> {
                                        HistoryScreen(
                                            records = historyRecords,
                                            speedUnit = speedUnit,
                                            language = language,
                                            onDeleteRecord = { viewModel.deleteRecord(it) },
                                            onClearAll = { viewModel.clearAllHistory() }
                                        )
                                    }

                                    NavTab.SETTINGS -> {
                                        SettingsScreen(
                                            language = language,
                                            speedUnit = speedUnit,
                                            selectedServer = selectedServer,
                                            isProPlan = isProPlan,
                                            autoSaveHistory = autoSaveHistory,
                                            reducedMotion = reducedMotion,
                                            batterySaver = batterySaver,
                                            currentTheme = currentTheme,
                                            rewardAdsEnabled = rewardAdsEnabled,
                                            personalizedAdsEnabled = personalizedAdsEnabled,
                                            onLanguageChange = { viewModel.setLanguage(it) },
                                            onSpeedUnitChange = { viewModel.setSpeedUnit(it) },
                                            onOpenServerModal = { viewModel.openServerModal() },
                                            onToggleAutoSave = { viewModel.toggleAutoSave() },
                                            onToggleReducedMotion = { viewModel.toggleReducedMotion() },
                                            onToggleBatterySaver = { viewModel.toggleBatterySaver() },
                                            onOpenUpgradeModal = { viewModel.openUpgradeModal() },
                                            onToggleRewardAds = { viewModel.toggleRewardAds() },
                                            onTogglePersonalizedAds = { viewModel.togglePersonalizedAds() },
                                            onClearAdCache = { viewModel.clearAdCache() },
                                            onThemeChange = { viewModel.setTheme(it) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Modals
        if (showUpgradeModal) {
            UpgradeModal(
                isProPlan = isProPlan,
                language = language,
                onDismiss = { viewModel.closeUpgradeModal() },
                onTogglePro = { viewModel.toggleProPlan() }
            )
        }

        if (showServerModal) {
            ServerSelectionModal(
                selectedServer = selectedServer,
                language = language,
                onDismiss = { viewModel.closeServerModal() },
                onSelectServer = { viewModel.setSelectedServer(it) }
            )
        }

        if (showAdModal) {
            AdRewardModal(
                language = language,
                onDismiss = { viewModel.closeAdModal() },
                onClaimReward = { viewModel.claimAdReward() }
            )
        }
    }
}
