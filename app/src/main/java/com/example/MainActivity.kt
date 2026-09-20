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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppScreen
import java.util.Locale
import com.example.model.NavTab
import com.example.ui.ZipspeedViewModel
import com.example.ui.components.BottomNavBar
import com.example.ui.components.AdaptiveSideNavigation
import com.example.ui.components.ServerSelectionModal
import com.example.ui.components.SecurityShieldModal
import com.example.ui.components.VipAdFreeModal
import com.example.ui.components.TopHeader
import com.example.ui.screens.AdFreeScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StatusScreen
import com.example.ui.screens.VideoScreen
import com.example.ui.theme.ZipspeedTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ZipspeedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
            ZipspeedTheme(theme = currentTheme) {
                ZipspeedMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ZipspeedMainApp(viewModel: ZipspeedViewModel) {
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val reducedMotion by viewModel.reducedMotion.collectAsStateWithLifecycle()
    val batterySaver by viewModel.batterySaver.collectAsStateWithLifecycle()
    val autoSaveHistory by viewModel.autoSaveHistory.collectAsStateWithLifecycle()
    val speedUnit by viewModel.speedUnit.collectAsStateWithLifecycle()
    val isPrecisionMode by viewModel.isPrecisionMode.collectAsStateWithLifecycle()
    val testState by viewModel.testState.collectAsStateWithLifecycle()
    val historyRecords by viewModel.historyRecords.collectAsStateWithLifecycle()
    val previousResult by viewModel.previousResult.collectAsStateWithLifecycle()
    val ipInfo by viewModel.ipInfo.collectAsStateWithLifecycle()

    val videoTestState by viewModel.videoTestState.collectAsStateWithLifecycle()
    val webTestState by viewModel.webTestState.collectAsStateWithLifecycle()

    val isProPlan by viewModel.isProPlan.collectAsStateWithLifecycle()
    val isGpsModeEnabled by viewModel.isGpsModeEnabled.collectAsStateWithLifecycle()
    val isSecurityShieldActive by viewModel.isSecurityShieldActive.collectAsStateWithLifecycle()
    val securityReport by viewModel.securityReport.collectAsStateWithLifecycle()
    val showSecurityModal by viewModel.showSecurityModal.collectAsStateWithLifecycle()
    val showVipModal by viewModel.showVipModal.collectAsStateWithLifecycle()
    val showServerModal by viewModel.showServerModal.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val locale = remember(language) { Locale(language.code) }
    val configuration = remember(locale, context) {
        android.content.res.Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
    }

    CompositionLocalProvider(
        LocalConfiguration provides configuration
    ) {
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
                        val screenWidthDp = LocalConfiguration.current.screenWidthDp
                        val isPhoneLayout = screenWidthDp < 600
                        val isDesktopLayout = screenWidthDp > 1200

                        Scaffold(
                            containerColor = Color.Transparent,
                            contentWindowInsets = WindowInsets.statusBars,
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                if (isPhoneLayout) {
                                    BottomNavBar(
                                        activeTab = activeTab,
                                        language = language,
                                        onTabSelected = { viewModel.selectTab(it) },
                                        isSecurityShieldActive = isSecurityShieldActive,
                                        onToggleSecurityShield = { viewModel.toggleSecurityShield(it) },
                                        onSecurityClick = { viewModel.openSecurityModal() }
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                if (!isPhoneLayout) {
                                    AdaptiveSideNavigation(
                                        activeTab = activeTab,
                                        language = language,
                                        expanded = isDesktopLayout,
                                        onTabSelected = { viewModel.selectTab(it) }
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                ) {
                                // Header Bar with Dark/Light mode, VIP button, and GPS
                                TopHeader(
                                    language = language,
                                    reducedMotion = reducedMotion,
                                    isDarkTheme = currentTheme.isDark,
                                    isVipAdFree = isProPlan,
                                    isGpsActive = isGpsModeEnabled,
                                    onLanguageChange = { viewModel.setLanguage(it) },
                                    onToggleDarkLight = { viewModel.toggleDarkLightMode() },
                                    onOpenVipModal = { viewModel.openVipModal() },
                                    onOpenSettings = { viewModel.selectTab(NavTab.SETTINGS) },
                                    onToggleGps = { viewModel.toggleGpsMode() }
                                )

                                // Tab Content View
                                AnimatedContent(
                                    targetState = activeTab,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    label = "tabTransition",
                                    modifier = Modifier.weight(1f)
                                ) { targetTab ->
                                    when (targetTab) {
                                        NavTab.SPEED -> {
                                            HomeScreen(
                                                testState = testState,
                                                selectedServer = selectedServer,
                                                ipInfo = ipInfo,
                                                speedUnit = speedUnit,
                                                language = language,
                                                reducedMotion = reducedMotion || batterySaver,
                                                isPrecisionMode = isPrecisionMode,
                                                isVipAdFree = isProPlan,
                                                isGpsActive = isGpsModeEnabled,
                                                previousResult = previousResult,
                                                videoState = videoTestState,
                                                webState = webTestState,
                                                onStartTest = { viewModel.startSpeedTest() },
                                                onStartPrecisionTest = { viewModel.startPrecisionSpeedTest() },
                                                onCancelTest = { viewModel.cancelSpeedTest() },
                                                onTogglePrecisionMode = { viewModel.togglePrecisionMode() },
                                                onStartVideoTest = { viewModel.startVideoTest() },
                                                onCancelVideoTest = { viewModel.cancelVideoTest() },
                                                onStartWebTest = { viewModel.startWebTest() },
                                                onCancelWebTest = { viewModel.cancelWebTest() },
                                                onOpenServerModal = { viewModel.openServerModal() },
                                                onSelectServer = { viewModel.setSelectedServer(it) },
                                                onRefreshIp = { viewModel.refreshIpInfo() },
                                                onToggleSpeedUnit = { viewModel.toggleSpeedUnit() },
                                                onToggleGpsMode = { viewModel.toggleGpsMode() },
                                                onOpenVipModal = { viewModel.openVipModal() },
                                                onNavigateToResults = { viewModel.selectTab(NavTab.HISTORY) },
                                                onNavigateToSettings = { viewModel.selectTab(NavTab.SETTINGS) }
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

                                        NavTab.VIDEO -> {
                                            VideoScreen(
                                                videoState = videoTestState,
                                                language = language,
                                                onStartTest = { viewModel.startVideoTest() },
                                                onCancelTest = { viewModel.cancelVideoTest() }
                                            )
                                        }

                                        NavTab.STATUS -> {
                                            StatusScreen(
                                                ipInfo = ipInfo,
                                                language = language,
                                                onRefreshIp = { viewModel.refreshIpInfo() }
                                            )
                                        }

                                        NavTab.MAP -> {
                                            MapScreen(
                                                selectedServer = selectedServer,
                                                language = language,
                                                onSelectServer = { viewModel.setSelectedServer(it) }
                                            )
                                        }

                                        NavTab.AD_FREE -> {
                                            AdFreeScreen(
                                                isVipAdFree = isProPlan,
                                                language = language,
                                                onActivateVip = { viewModel.activateVipAdFree() }
                                            )
                                        }

                                        NavTab.SETTINGS -> {
                                            SettingsScreen(
                                                language = language,
                                                speedUnit = speedUnit,
                                                selectedServer = selectedServer,
                                                autoSaveHistory = autoSaveHistory,
                                                reducedMotion = reducedMotion,
                                                batterySaver = batterySaver,
                                                onLanguageChange = { viewModel.setLanguage(it) },
                                                onSpeedUnitChange = { viewModel.setSpeedUnit(it) },
                                                onOpenServerModal = { viewModel.openServerModal() },
                                                onToggleAutoSave = { viewModel.toggleAutoSave() },
                                                onToggleReducedMotion = { viewModel.toggleReducedMotion() },
                                                onToggleBatterySaver = { viewModel.toggleBatterySaver() },
                                                isDarkTheme = currentTheme.isDark,
                                                isSecurityShieldActive = isSecurityShieldActive,
                                                onToggleDarkLight = { viewModel.toggleDarkLightMode() },
                                                onToggleSecurityShield = { viewModel.toggleSecurityShield(it) },
                                                onOpenSecurityModal = { viewModel.openSecurityModal() }
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
            if (showServerModal) {
                ServerSelectionModal(
                    selectedServer = selectedServer,
                    language = language,
                    onDismiss = { viewModel.closeServerModal() },
                    onSelectServer = { viewModel.setSelectedServer(it) }
                )
            }

            if (showSecurityModal) {
                SecurityShieldModal(
                    securityReport = securityReport,
                    isShieldEnabled = isSecurityShieldActive,
                    onToggleShield = { viewModel.toggleSecurityShield(it) },
                    onDismiss = { viewModel.closeSecurityModal() }
                )
            }

            if (showVipModal) {
                VipAdFreeModal(
                    isVipAdFree = isProPlan,
                    onDismiss = { viewModel.closeVipModal() },
                    onPurchaseVip = { viewModel.purchaseVipAdFree(it) },
                    onWatchAdForTempVip = { viewModel.watchAdForTempVip() }
                )
            }
        }
    }
}
