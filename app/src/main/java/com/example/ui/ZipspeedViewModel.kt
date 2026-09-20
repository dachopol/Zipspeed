package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SpeedTestRecord
import com.example.data.SpeedTestRepository
import com.example.engine.IpInfoFetcher
import com.example.engine.NetworkIpInfo
import com.example.engine.NetworkSpeedTester
import com.example.model.DEFAULT_SERVERS
import com.example.model.Language
import com.example.model.NavTab
import com.example.model.ServerInfo
import com.example.model.SpeedTestState
import com.example.model.SpeedUnit
import com.example.model.TestPhase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.ui.theme.ThemeManager
import com.example.ui.theme.ThemeDefinition
import com.example.engine.GpsLocationHelper
import com.example.engine.AntiTamperSecurityEngine
import com.example.engine.SecurityThreatReport

import com.example.engine.ScannerScheduleEvaluator
import com.example.engine.SignalAlertNotificationManager
import com.example.engine.VideoPerformanceTester
import com.example.engine.WebPerformanceTester
import com.example.engine.WifiSignalReader
import com.example.model.DowndetectorService
import com.example.model.IspComparisonBenchmark
import com.example.model.MobilePerformanceState
import com.example.model.OutageLevel
import com.example.model.ScannerExecutionStatus
import com.example.model.ScannerScheduleConfig
import com.example.model.SchedulePreset
import com.example.model.SignalAlertEvent
import com.example.model.SignalMapPoint
import com.example.model.SignalScannerState
import com.example.model.SubTestMode
import com.example.model.VideoTestState
import com.example.model.WebTestState

class ZipspeedViewModel(application: Application) : AndroidViewModel(application) {

    // Theme Management - Default soft eye-comfort theme
    val themeManager = ThemeManager(application.applicationContext)
    val currentTheme: StateFlow<ThemeDefinition> = themeManager.currentTheme

    fun setTheme(themeId: String) {
        themeManager.setTheme(themeId)
    }

    fun toggleTheme() {
        themeManager.toggleNextTheme()
    }

    private val repository: SpeedTestRepository
    val historyRecords: StateFlow<List<SpeedTestRecord>>
    val previousResult: StateFlow<SpeedTestRecord?>

    private val tester = NetworkSpeedTester()
    val testState: StateFlow<SpeedTestState> = tester.state

    // Video Streaming Tester
    private val videoTester = VideoPerformanceTester()
    val videoTestState: StateFlow<VideoTestState> = videoTester.state
    private var activeVideoJob: Job? = null

    // Web Browsing & CDN Tester
    private val webTester = WebPerformanceTester()
    val webTestState: StateFlow<WebTestState> = webTester.state
    private var activeWebJob: Job? = null

    // Mobile Device & Connection Health State
    private val _mobileState = MutableStateFlow(MobilePerformanceState())
    val mobileState: StateFlow<MobilePerformanceState> = _mobileState.asStateFlow()

    // Downdetector Services
    private val initialOutageServices = emptyList<DowndetectorService>()

    private val _downdetectorServices = MutableStateFlow(initialOutageServices)
    val downdetectorServices: StateFlow<List<DowndetectorService>> = _downdetectorServices.asStateFlow()

    // Regional benchmark data must come from a real source before display.
    private val _selectedRegion = MutableStateFlow("ไม่ระบุ")
    val selectedRegion: StateFlow<String> = _selectedRegion.asStateFlow()

    private val _ispBenchmarks = MutableStateFlow(emptyList<IspComparisonBenchmark>())
    val ispBenchmarks: StateFlow<List<IspComparisonBenchmark>> = _ispBenchmarks.asStateFlow()

    // No synthetic heatmap points: spatial points are empty until a real measurement source exists.
    private val initialMovementPoints = emptyList<SignalMapPoint>()

    private val _signalScannerState = MutableStateFlow(
        SignalScannerState(
            currentDbm = 0,
            channelWidthMhz = 0,
            channel = 0,
            band = "ไม่มีข้อมูล",
            linkSpeedMbps = 0,
            movementPoints = initialMovementPoints,
            activeZone = "ไม่มีข้อมูล Wi-Fi จริง"
        )
    )
    val signalScannerState: StateFlow<SignalScannerState> = _signalScannerState.asStateFlow()

    private var activeWalkJob: Job? = null

    fun toggleWalkSimulation() {
        if (_signalScannerState.value.isRecordingMovement) {
            activeWalkJob?.cancel()
            activeWalkJob = null
            _signalScannerState.update { it.copy(isRecordingMovement = false) }
            return
        }

        _signalScannerState.update { it.copy(isRecordingMovement = true) }
        activeWalkJob = viewModelScope.launch {
            while (_signalScannerState.value.isRecordingMovement) {
                refreshSignalScan()
                delay(1500)
            }
        }
    }

    fun pinCurrentLocation() {
        // Indoor X/Y cannot be inferred reliably from Android network APIs.
        // Refresh the real radio reading instead of generating a fake map coordinate.
        refreshSignalScan()
    }

    fun resetMovementMap() {
        activeWalkJob?.cancel()
        activeWalkJob = null
        _signalScannerState.update {
            it.copy(
                isRecordingMovement = false,
                movementPoints = initialMovementPoints
            )
        }
    }

    fun clearMovementPoints() {
        activeWalkJob?.cancel()
        activeWalkJob = null
        _signalScannerState.update {
            it.copy(
                isRecordingMovement = false,
                movementPoints = emptyList()
            )
        }
    }

    fun toggleSignalAlertNotifications() {
        _signalScannerState.update { it.copy(alertNotificationsEnabled = !it.alertNotificationsEnabled) }
    }

    fun setSignalAlertThreshold(thresholdDbm: Int) {
        _signalScannerState.update { it.copy(alertThresholdDbm = thresholdDbm) }
    }

    fun sendTestSignalAlert() {
        val state = _signalScannerState.value
        val testDbm = -89
        val testZone = "ห้องน้ำในตัว (จุดอับสัญญาณ)"
        SignalAlertNotificationManager.checkAndNotifyPoorSignal(
            context = getApplication(),
            dbm = testDbm,
            zoneName = testZone,
            thresholdDbm = state.alertThresholdDbm,
            forceTest = true
        )
        val alertEvent = SignalAlertEvent(
            dbm = testDbm,
            zoneName = testZone,
            threshold = state.alertThresholdDbm
        )
        _signalScannerState.update { s ->
            s.copy(
                lastAlertEvent = alertEvent,
                alertHistory = (listOf(alertEvent) + s.alertHistory).take(20)
            )
        }
    }

    fun dismissCurrentAlert() {
        _signalScannerState.update { it.copy(lastAlertEvent = null) }
    }

    private fun updateExecutionStatus() {
        val s = _signalScannerState.value
        val status = ScannerScheduleEvaluator.evaluateStatus(
            backgroundScanEnabled = s.backgroundScanEnabled,
            config = s.scheduleConfig,
            batteryPercent = s.currentBatteryPercent,
            isCharging = s.isDeviceCharging
        )
        _signalScannerState.update { it.copy(executionStatus = status) }
    }

    fun toggleScannerScheduleEnabled() {
        _signalScannerState.update {
            val updatedConfig = it.scheduleConfig.copy(scheduleEnabled = !it.scheduleConfig.scheduleEnabled)
            it.copy(scheduleConfig = updatedConfig)
        }
        updateExecutionStatus()
    }

    fun setSchedulePreset(preset: SchedulePreset) {
        _signalScannerState.update {
            val updatedConfig = it.scheduleConfig.copy(preset = preset)
            it.copy(scheduleConfig = updatedConfig)
        }
        updateExecutionStatus()
    }

    fun setCustomScheduleHours(startH: Int, endH: Int) {
        _signalScannerState.update {
            val updatedConfig = it.scheduleConfig.copy(
                preset = SchedulePreset.CUSTOM,
                startHour = startH.coerceIn(0, 23),
                endHour = endH.coerceIn(0, 23)
            )
            it.copy(scheduleConfig = updatedConfig)
        }
        updateExecutionStatus()
    }

    fun setMinBatteryThreshold(percent: Int) {
        _signalScannerState.update {
            val updatedConfig = it.scheduleConfig.copy(minBatteryThresholdPercent = percent)
            it.copy(scheduleConfig = updatedConfig)
        }
        updateExecutionStatus()
    }

    fun toggleBypassBatteryWhenCharging() {
        _signalScannerState.update {
            val updatedConfig = it.scheduleConfig.copy(
                bypassBatteryWhenCharging = !it.scheduleConfig.bypassBatteryWhenCharging
            )
            it.copy(scheduleConfig = updatedConfig)
        }
        updateExecutionStatus()
    }

    fun setSimulatedBatteryLevel(percent: Int) {
        refreshBatteryStatus()
    }

    fun toggleSimulatedCharging() {
        refreshBatteryStatus()
    }

    fun refreshBatteryStatus() {
        val battery = ScannerScheduleEvaluator.getDeviceBatteryStatus(getApplication())
        _signalScannerState.update {
            it.copy(
                currentBatteryPercent = battery.levelPercent,
                isDeviceCharging = battery.isCharging
            )
        }
        updateExecutionStatus()
    }

    // Ad Management State
    private val _rewardAdsEnabled = MutableStateFlow(true)
    val rewardAdsEnabled: StateFlow<Boolean> = _rewardAdsEnabled.asStateFlow()

    private val _personalizedAdsEnabled = MutableStateFlow(false)
    val personalizedAdsEnabled: StateFlow<Boolean> = _personalizedAdsEnabled.asStateFlow()

    fun toggleRewardAds() {
        _rewardAdsEnabled.update { !it }
    }

    fun togglePersonalizedAds() {
        _personalizedAdsEnabled.update { !it }
    }

    fun clearAdCache() {
        // Clear cached ad instances and temp telemetry
    }

    fun reportOutage(serviceId: String) {
        _downdetectorServices.update { list ->
            list.map { service ->
                if (service.id == serviceId) {
                    service.copy(
                        incidentCount24h = service.incidentCount24h + 1,
                        userReported = true
                    )
                } else service
            }
        }
    }

    fun selectRegion(region: String) {
        _selectedRegion.value = region
        _ispBenchmarks.value = emptyList()
    }

    private var activeBackgroundScanJob: Job? = null

    fun toggleBackgroundScan() {
        val willEnable = !_signalScannerState.value.backgroundScanEnabled
        _signalScannerState.update { it.copy(backgroundScanEnabled = willEnable) }
        updateExecutionStatus()

        activeBackgroundScanJob?.cancel()
        activeBackgroundScanJob = null

        if (willEnable) {
            activeBackgroundScanJob = viewModelScope.launch {
                while (_signalScannerState.value.backgroundScanEnabled) {
                    refreshBatteryStatus()
                    updateExecutionStatus()
                    if (_signalScannerState.value.executionStatus == ScannerExecutionStatus.RUNNING) {
                        refreshSignalScan()
                    }
                    delay(3500)
                }
            }
        }
    }

    fun refreshSignalScan() {
        val snapshot = WifiSignalReader.read(getApplication())
        if (snapshot == null) {
            _signalScannerState.update {
                it.copy(
                    currentDbm = 0,
                    channelWidthMhz = 0,
                    channel = 0,
                    band = "ไม่มีข้อมูล",
                    linkSpeedMbps = 0,
                    activeZone = "ไม่มีข้อมูล Wi-Fi จริง"
                )
            }
            return
        }

        val zone = "Wi-Fi ปัจจุบัน"
        _signalScannerState.update {
            it.copy(
                currentDbm = snapshot.rssiDbm,
                band = snapshot.band,
                linkSpeedMbps = snapshot.linkSpeedMbps,
                activeZone = zone
            )
        }

        val state = _signalScannerState.value
        if (state.alertNotificationsEnabled && snapshot.rssiDbm <= state.alertThresholdDbm) {
            SignalAlertNotificationManager.checkAndNotifyPoorSignal(
                context = getApplication(),
                dbm = snapshot.rssiDbm,
                zoneName = zone,
                thresholdDbm = state.alertThresholdDbm
            )
            val alertEvent = SignalAlertEvent(
                dbm = snapshot.rssiDbm,
                zoneName = zone,
                threshold = state.alertThresholdDbm
            )
            _signalScannerState.update { s ->
                s.copy(
                    lastAlertEvent = alertEvent,
                    alertHistory = (listOf(alertEvent) + s.alertHistory).take(20)
                )
            }
        }
    }

    fun startVideoTest() {
        if (videoTestState.value.isTesting) return
        cancelSpeedTest()
        cancelWebTest()
        activeVideoJob?.cancel()
        activeVideoJob = viewModelScope.launch {
            videoTester.runVideoTest(_batterySaver.value)
        }
    }

    fun cancelVideoTest() {
        activeVideoJob?.cancel()
        activeVideoJob = null
        videoTester.cancelTest()
    }

    fun startWebTest() {
        if (webTestState.value.isTesting) return
        cancelSpeedTest()
        cancelVideoTest()
        activeWebJob?.cancel()
        activeWebJob = viewModelScope.launch {
            webTester.runWebTest()
        }
    }

    fun cancelWebTest() {
        activeWebJob?.cancel()
        activeWebJob = null
        webTester.cancelTest()
    }

    fun refreshMobileScan() {
        val wifi = WifiSignalReader.read(getApplication())
        val battery = ScannerScheduleEvaluator.getDeviceBatteryStatus(getApplication())
        _mobileState.update {
            it.copy(
                batteryLevel = battery.levelPercent,
                signalStrengthDbm = wifi?.rssiDbm ?: 0,
                signalDbm = wifi?.rssiDbm ?: 0,
                networkType = wifi?.band ?: "ไม่มีข้อมูล",
                deviceTemperatureC = 0.0,
                bufferbloatMs = 0
            )
        }
    }

    private val ipFetcher = IpInfoFetcher()
    private val _ipInfo = MutableStateFlow(NetworkIpInfo())
    val ipInfo: StateFlow<NetworkIpInfo> = _ipInfo.asStateFlow()

    private var activeTestJob: Job? = null

    // Flow Navigation & Auth State - Default directly to MAIN so all tabs are immediately accessible!
    private val _currentScreen = MutableStateFlow(com.example.model.AppScreen.MAIN)
    val currentScreen: StateFlow<com.example.model.AppScreen> = _currentScreen.asStateFlow()

    private val _userProfile = MutableStateFlow<com.example.model.UserProfile?>(null)
    val userProfile: StateFlow<com.example.model.UserProfile?> = _userProfile.asStateFlow()

    private val _showAdModal = MutableStateFlow(false)
    val showAdModal: StateFlow<Boolean> = _showAdModal.asStateFlow()

    // UI Preferences
    private val _activeTab = MutableStateFlow(NavTab.SPEED)
    val activeTab: StateFlow<NavTab> = _activeTab.asStateFlow()

    private var userHasManuallySelectedLanguage = false

    // Auto-detect system language by default upon app launch (Thai for TH system locale, English fallback for all others)
    private fun detectInitialSystemLanguage(): Language {
        val sysLang = java.util.Locale.getDefault().language.lowercase()
        return if (sysLang.startsWith("th")) Language.TH else Language.EN
    }

    private val _language = MutableStateFlow(detectInitialSystemLanguage())
    val language: StateFlow<Language> = _language.asStateFlow()

    private val _isProPlan = MutableStateFlow(false)
    val isProPlan: StateFlow<Boolean> = _isProPlan.asStateFlow()

    private val _selectedServer = MutableStateFlow(DEFAULT_SERVERS.first())
    val selectedServer: StateFlow<ServerInfo> = _selectedServer.asStateFlow()

    private val _reducedMotion = MutableStateFlow(false)
    val reducedMotion: StateFlow<Boolean> = _reducedMotion.asStateFlow()

    private val _batterySaver = MutableStateFlow(false)
    val batterySaver: StateFlow<Boolean> = _batterySaver.asStateFlow()

    private val _autoSaveHistory = MutableStateFlow(true)
    val autoSaveHistory: StateFlow<Boolean> = _autoSaveHistory.asStateFlow()

    private val _speedUnit = MutableStateFlow(SpeedUnit.MBPS)
    val speedUnit: StateFlow<SpeedUnit> = _speedUnit.asStateFlow()

    private val _isPrecisionMode = MutableStateFlow(false)
    val isPrecisionMode: StateFlow<Boolean> = _isPrecisionMode.asStateFlow()

    private val _showUpgradeModal = MutableStateFlow(false)
    val showUpgradeModal: StateFlow<Boolean> = _showUpgradeModal.asStateFlow()

    private val _showServerModal = MutableStateFlow(false)
    val showServerModal: StateFlow<Boolean> = _showServerModal.asStateFlow()

    // Speed Test Limit (Free Plan 3K quota)
    private val prefs = application.getSharedPreferences("zipspeed_prefs", android.content.Context.MODE_PRIVATE)
    private val _usedTestCount = MutableStateFlow(prefs.getInt("used_test_count", 0))
    val usedTestCount: StateFlow<Int> = _usedTestCount.asStateFlow()

    val maxTestLimit: Int = 3000

    fun resetLimit() {
        _usedTestCount.value = 0
        prefs.edit().putInt("used_test_count", 0).apply()
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SpeedTestRepository(database.speedTestDao())
        historyRecords = repository.allRecords.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        previousResult = repository.allRecords.map { it.firstOrNull() }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
        refreshIpInfo()
        refreshBatteryStatus()
    }

    fun refreshIpInfo() {
        viewModelScope.launch {
            _ipInfo.update { it.copy(isFetching = true) }
            val info = ipFetcher.fetchPublicIpInfo()
            _ipInfo.value = info

            // Auto-detect country location from IP if user hasn't manually selected language
            if (!userHasManuallySelectedLanguage && !info.countryCode.isNullOrBlank()) {
                val sysLang = java.util.Locale.getDefault().language.lowercase()
                if (info.countryCode.equals("TH", ignoreCase = true) && sysLang.startsWith("th")) {
                    _language.value = Language.TH
                } else if (!info.countryCode.equals("TH", ignoreCase = true)) {
                    _language.value = Language.EN
                }
            }
        }
    }

    fun selectTab(tab: NavTab) {
        _activeTab.value = tab
    }

    fun setLanguage(lang: Language) {
        userHasManuallySelectedLanguage = true
        _language.value = lang
    }

    fun setSelectedServer(server: ServerInfo) {
        _selectedServer.value = server
        _showServerModal.value = false
    }

    fun selectServerAndRun(server: ServerInfo) {
        _selectedServer.value = server
        _showServerModal.value = false
        startSpeedTest()
    }

    fun toggleProPlan() {
        // Premium entitlement must never be toggled locally.
        _showVipModal.value = true
    }

    fun setProPlan(isPro: Boolean) {
        if (!isPro) {
            _isProPlan.value = false
            prefs.edit().putBoolean("is_vip_ad_free", false).apply()
        } else {
            _showVipModal.value = true
        }
    }

    // GPS & ISP Region Tagging Integration
    private val gpsHelper = GpsLocationHelper(application.applicationContext)
    private val _isGpsModeEnabled = MutableStateFlow(false)
    val isGpsModeEnabled: StateFlow<Boolean> = _isGpsModeEnabled.asStateFlow()

    fun toggleGpsMode() {
        val next = !_isGpsModeEnabled.value
        _isGpsModeEnabled.value = next
        if (next) {
            fetchGpsCoordinates()
        } else {
            _ipInfo.update {
                it.copy(
                    isGpsActive = false,
                    locationStatusText = "โหมด GPS: ปิดใช้งาน (ใช้ข้อมูล Anycast โหนด)"
                )
            }
        }
    }

    fun fetchGpsCoordinates() {
        viewModelScope.launch {
            _ipInfo.update { it.copy(isFetching = true) }
            val gpsData = gpsHelper.getCurrentLocation()
            if (gpsData != null) {
                _ipInfo.update {
                    it.copy(
                        isFetching = false,
                        latitude = gpsData.latitude,
                        longitude = gpsData.longitude,
                        regionTag = gpsData.regionTag,
                        province = gpsData.province,
                        isGpsActive = true,
                        locationStatusText = "GPS: เปิดใช้งาน (${gpsData.province} • พิกัดจริง)"
                    )
                }
            } else {
                _ipInfo.update {
                    it.copy(
                        isFetching = false,
                        isGpsActive = false,
                        locationStatusText = if (!gpsHelper.hasLocationPermission()) {
                            "GPS: ยังไม่ได้รับสิทธิ์เข้าถึงพิกัด (แตะเพื่อขอสิทธิ์)"
                        } else {
                            "GPS: ไม่สามารถระบุพิกัดได้ (สัญญาณไม่พร้อม)"
                        }
                    )
                }
            }
        }
    }

    // Anti-Tamper Security Engine & Shield
    private val _isSecurityShieldActive = MutableStateFlow(true)
    val isSecurityShieldActive: StateFlow<Boolean> = _isSecurityShieldActive.asStateFlow()

    private val _securityReport = MutableStateFlow(
        AntiTamperSecurityEngine.runDeepSecurityScan(application.applicationContext, true)
    )
    val securityReport: StateFlow<SecurityThreatReport> = _securityReport.asStateFlow()

    private val _showSecurityModal = MutableStateFlow(false)
    val showSecurityModal: StateFlow<Boolean> = _showSecurityModal.asStateFlow()

    fun toggleSecurityShield(enabled: Boolean) {
        _isSecurityShieldActive.value = enabled
        _securityReport.value = AntiTamperSecurityEngine.runDeepSecurityScan(
            getApplication<Application>().applicationContext,
            enabled
        )
    }

    fun openSecurityModal() {
        _securityReport.value = AntiTamperSecurityEngine.runDeepSecurityScan(
            getApplication<Application>().applicationContext,
            _isSecurityShieldActive.value
        )
        _showSecurityModal.value = true
    }

    fun closeSecurityModal() {
        _showSecurityModal.value = false
    }

    // VIP Ad-Free Modal & Monetization
    private val _showVipModal = MutableStateFlow(false)
    val showVipModal: StateFlow<Boolean> = _showVipModal.asStateFlow()

    fun openVipModal() {
        _showVipModal.value = true
    }

    fun closeVipModal() {
        _showVipModal.value = false
    }

    fun purchaseVipAdFree(planName: String) {
        // Entitlement is never granted locally. Real Google Play Billing must verify a purchase first.
        _showVipModal.value = true
    }

    fun activateVipAdFree() {
        if (!_isProPlan.value) {
            _showVipModal.value = true
        }
    }

    fun watchAdForTempVip() {
        // Rewarded entitlement must only be granted from a real rewarded-ad completion callback.
        _showVipModal.value = true
    }

    fun toggleDarkLightMode() {
        themeManager.toggleDarkLight()
    }

    fun toggleReducedMotion() {
        _reducedMotion.update { !it }
    }

    fun toggleBatterySaver() {
        _batterySaver.update { !it }
    }

    fun toggleAutoSave() {
        _autoSaveHistory.update { !it }
    }

    fun setSpeedUnit(unit: SpeedUnit) {
        _speedUnit.value = unit
    }

    fun toggleSpeedUnit() {
        _speedUnit.update { if (it == SpeedUnit.MBPS) SpeedUnit.MB_S else SpeedUnit.MBPS }
    }

    fun openUpgradeModal() {
        _showUpgradeModal.value = true
    }

    fun closeUpgradeModal() {
        _showUpgradeModal.value = false
    }

    fun openServerModal() {
        _showServerModal.value = true
    }

    fun closeServerModal() {
        _showServerModal.value = false
    }

    fun togglePrecisionMode() {
        _isPrecisionMode.update { !it }
    }

    fun setPrecisionMode(enabled: Boolean) {
        _isPrecisionMode.value = enabled
    }

    fun startPrecisionSpeedTest() {
        if (testState.value.phase == TestPhase.TESTING_PING ||
            testState.value.phase == TestPhase.TESTING_DOWNLOAD ||
            testState.value.phase == TestPhase.TESTING_UPLOAD
        ) {
            return
        }
        _isPrecisionMode.value = true
        startSpeedTest()
    }

    fun startSpeedTest() {
        val currentPhase = testState.value.phase
        if (currentPhase == TestPhase.TESTING_PING ||
            currentPhase == TestPhase.TESTING_DOWNLOAD ||
            currentPhase == TestPhase.TESTING_UPLOAD
        ) {
            return
        }

        // Limit check for Free plan (3K tests limit)
        if (!_isProPlan.value && _usedTestCount.value >= maxTestLimit) {
            _showUpgradeModal.value = true
            return
        }

        cancelVideoTest()
        cancelWebTest()

        if (_isGpsModeEnabled.value) {
            fetchGpsCoordinates()
        }

        activeTestJob?.cancel()
        activeTestJob = viewModelScope.launch {
            val server = _selectedServer.value
            val isPro = _isProPlan.value
            val isBatterySaver = _batterySaver.value
            val isPrecision = _isPrecisionMode.value

            val result = tester.runSpeedTest(
                server = server,
                isPrecisionMode = isPrecision,
                isPro = isPro,
                batterySaver = isBatterySaver
            )

            // Dynamically sync detected Anycast edge details if available
            if (result.detectedColo != null || result.detectedClientIp != null) {
                _ipInfo.update { current ->
                    current.copy(
                        publicIp = result.detectedClientIp ?: current.publicIp,
                        colo = result.detectedColo ?: current.colo,
                        city = if (current.city.isNullOrBlank() && result.detectedColo != null) "Cloudflare ${result.detectedColo}" else current.city,
                        ispName = if (result.detectedAsn != null) "AS${result.detectedAsn} Network" else current.ispName
                    )
                }
            }

            if (result.phase == TestPhase.COMPLETED) {
                // Increment test usage count
                val newCount = _usedTestCount.value + 1
                _usedTestCount.value = newCount
                prefs.edit().putInt("used_test_count", newCount).apply()

                if (_autoSaveHistory.value) {
                    val download = result.downloadMbps
                    val upload = result.uploadMbps
                    val ping = result.pingMs
                    val jitter = result.jitterMs
                    if (download != null && upload != null && ping != null && jitter != null) {
                        val wifi = WifiSignalReader.read(getApplication())
                        val record = SpeedTestRecord(
                            downloadMbps = download,
                            uploadMbps = upload,
                            pingMs = ping,
                            jitterMs = jitter,
                            packetLossPercent = result.packetLossPercent,
                            serverName = server.name,
                            serverLocation = result.detectedColo?.let { "Cloudflare $it PoP" } ?: "Anycast (PoP ไม่ทราบ)",
                            networkType = wifi?.band ?: "ไม่ทราบ"
                        )
                        repository.insertRecord(record)
                    }
                }
            }
        }
    }

    fun cancelSpeedTest() {
        activeTestJob?.cancel()
        activeTestJob = null
        tester.cancelTest()
    }

    fun resetSpeedTest() {
        activeTestJob?.cancel()
        activeTestJob = null
        tester.reset()
    }

    fun deleteRecord(record: SpeedTestRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun startApp() {
        _currentScreen.value = com.example.model.AppScreen.LOGIN
    }

    fun loginUser(profile: com.example.model.UserProfile) {
        _userProfile.value = profile
        _currentScreen.value = com.example.model.AppScreen.MAIN
    }

    fun skipLogin() {
        _userProfile.value = com.example.model.UserProfile(
            name = "Guest User",
            email = "guest@zipspeed.app",
            provider = "Guest",
            isGuest = true
        )
        _currentScreen.value = com.example.model.AppScreen.MAIN
    }

    fun openAdModal() {
        _showAdModal.value = true
    }

    fun closeAdModal() {
        _showAdModal.value = false
    }

    fun claimAdReward() {
        _isProPlan.value = true
        _showAdModal.value = false
    }
}
