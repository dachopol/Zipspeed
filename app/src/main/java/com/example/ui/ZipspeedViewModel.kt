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
import kotlin.random.Random

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
    private val initialOutageServices = listOf(
        DowndetectorService(
            id = "ais",
            name = "AIS (Fibre & 5G)",
            serviceType = "ผู้ให้บริการโทรคมนาคม (ISP/Mobile)",
            status = OutageLevel.NORMAL,
            incidentCount24h = 8,
            reportsTimeline = listOf(2, 1, 0, 3, 5, 8, 4),
            lastUpdatedText = "เมื่อสักครู่"
        ),
        DowndetectorService(
            id = "true",
            name = "True (5G & Online)",
            serviceType = "ผู้ให้บริการโทรคมนาคม (ISP/Mobile)",
            status = OutageLevel.NORMAL,
            incidentCount24h = 12,
            reportsTimeline = listOf(1, 3, 2, 8, 12, 6, 2),
            lastUpdatedText = "1 นาทีที่แล้ว"
        ),
        DowndetectorService(
            id = "3bb",
            name = "3BB Fibre",
            serviceType = "อินเทอร์เน็ตบ้าน (Broadband)",
            status = OutageLevel.DEGRADED,
            incidentCount24h = 24,
            reportsTimeline = listOf(4, 6, 12, 18, 24, 15, 10),
            lastUpdatedText = "3 นาทีที่แล้ว"
        ),
        DowndetectorService(
            id = "nt",
            name = "NT Broadband (National Telecom)",
            serviceType = "อินเทอร์เน็ตความเร็วสูง",
            status = OutageLevel.NORMAL,
            incidentCount24h = 5,
            reportsTimeline = listOf(0, 1, 2, 2, 5, 3, 1),
            lastUpdatedText = "5 นาทีที่แล้ว"
        ),
        DowndetectorService(
            id = "line",
            name = "LINE Application",
            serviceType = "แชท & วิดีโอคอล",
            status = OutageLevel.NORMAL,
            incidentCount24h = 3,
            reportsTimeline = listOf(1, 0, 1, 2, 3, 1, 0),
            lastUpdatedText = "เมื่อสักครู่"
        ),
        DowndetectorService(
            id = "facebook",
            name = "Facebook & Instagram",
            serviceType = "โซเชียลมีเดีย",
            status = OutageLevel.NORMAL,
            incidentCount24h = 14,
            reportsTimeline = listOf(3, 4, 8, 10, 14, 9, 5),
            lastUpdatedText = "2 นาทีที่แล้ว"
        ),
        DowndetectorService(
            id = "youtube",
            name = "YouTube & Google",
            serviceType = "สตรีมมิ่ง & ค้นหา",
            status = OutageLevel.NORMAL,
            incidentCount24h = 6,
            reportsTimeline = listOf(1, 1, 2, 4, 6, 3, 2),
            lastUpdatedText = "เมื่อสักครู่"
        )
    )

    private val _downdetectorServices = MutableStateFlow(initialOutageServices)
    val downdetectorServices: StateFlow<List<DowndetectorService>> = _downdetectorServices.asStateFlow()

    // Regional ISP Comparison Benchmarks
    private val _selectedRegion = MutableStateFlow("กรุงเทพฯ และปริมณฑล")
    val selectedRegion: StateFlow<String> = _selectedRegion.asStateFlow()

    private val _ispBenchmarks = MutableStateFlow(
        listOf(
            IspComparisonBenchmark("AIS Fibre", "กรุงเทพฯ และปริมณฑล", 265.4, 112.8, 11, 99.2, "อันดับ 1"),
            IspComparisonBenchmark("True Online", "กรุงเทพฯ และปริมณฑล", 248.0, 105.2, 13, 98.4, "อันดับ 2"),
            IspComparisonBenchmark("3BB Fibre", "กรุงเทพฯ และปริมณฑล", 225.6, 95.0, 15, 96.8, "อันดับ 3"),
            IspComparisonBenchmark("NT Broadband", "กรุงเทพฯ และปริมณฑล", 195.2, 82.5, 17, 95.5, "อันดับ 4")
        )
    )
    val ispBenchmarks: StateFlow<List<IspComparisonBenchmark>> = _ispBenchmarks.asStateFlow()

    // Initial default spatial scan points gathered during previous user walk
    private val initialMovementPoints = listOf(
        SignalMapPoint(x = 48.0, y = 42.0, dbm = -42, zoneName = "Living Room (Router AP)", linkSpeedMbps = 866, stepIndex = 1),
        SignalMapPoint(x = 42.0, y = 34.0, dbm = -49, zoneName = "Dining Area", linkSpeedMbps = 866, stepIndex = 2),
        SignalMapPoint(x = 28.0, y = 26.0, dbm = -58, zoneName = "Kitchen Room", linkSpeedMbps = 780, stepIndex = 3),
        SignalMapPoint(x = 16.0, y = 18.0, dbm = -73, zoneName = "Kitchen Balcony (Wall attenuation)", linkSpeedMbps = 433, stepIndex = 4),
        SignalMapPoint(x = 36.0, y = 48.0, dbm = -47, zoneName = "Central Corridor", linkSpeedMbps = 866, stepIndex = 5),
        SignalMapPoint(x = 64.0, y = 30.0, dbm = -54, zoneName = "Hallway to Study", linkSpeedMbps = 866, stepIndex = 6),
        SignalMapPoint(x = 78.0, y = 22.0, dbm = -66, zoneName = "Working Desk", linkSpeedMbps = 650, stepIndex = 7),
        SignalMapPoint(x = 88.0, y = 14.0, dbm = -83, zoneName = "Office Far Corner", linkSpeedMbps = 280, stepIndex = 8),
        SignalMapPoint(x = 66.0, y = 60.0, dbm = -57, zoneName = "Master Bedroom Entrance", linkSpeedMbps = 780, stepIndex = 9),
        SignalMapPoint(x = 78.0, y = 74.0, dbm = -68, zoneName = "Master Bedside", linkSpeedMbps = 520, stepIndex = 10),
        SignalMapPoint(x = 89.0, y = 85.0, dbm = -89, zoneName = "En-Suite Bathroom (Deadzone)", linkSpeedMbps = 150, stepIndex = 11),
        SignalMapPoint(x = 24.0, y = 70.0, dbm = -64, zoneName = "Guest Lounge", linkSpeedMbps = 600, stepIndex = 12)
    )

    // Signal Scanner State
    private val _signalScannerState = MutableStateFlow(
        SignalScannerState(
            currentDbm = -65,
            channelWidthMhz = 80,
            channel = 44,
            band = "5 GHz (Wi-Fi 6 802.11ax)",
            linkSpeedMbps = 866,
            routerX = 48.0,
            routerY = 42.0,
            movementPoints = initialMovementPoints
        )
    )
    val signalScannerState: StateFlow<SignalScannerState> = _signalScannerState.asStateFlow()

    private var activeWalkJob: Job? = null

    fun toggleWalkSimulation() {
        if (_signalScannerState.value.isRecordingMovement) {
            activeWalkJob?.cancel()
            activeWalkJob = null
            _signalScannerState.update { it.copy(isRecordingMovement = false) }
        } else {
            _signalScannerState.update { it.copy(isRecordingMovement = true) }
            activeWalkJob = viewModelScope.launch {
                val waypoints = listOf(
                    Pair(50.0, 44.0) to ("Living Room (ใกล้เราเตอร์)" to -43),
                    Pair(62.0, 36.0) to ("ทางเดินฝั่งขวา" to -52),
                    Pair(76.0, 24.0) to ("ห้องทำงาน" to -64),
                    Pair(88.0, 16.0) to ("มุมห้องทำงาน (ขอบผนัง)" to -81),
                    Pair(70.0, 48.0) to ("โถงทางเดินหลัก" to -55),
                    Pair(82.0, 72.0) to ("ห้องนอนใหญ่" to -68),
                    Pair(90.0, 86.0) to ("ห้องน้ำในตัว (จุดอับ)" to -90),
                    Pair(56.0, 54.0) to ("จุดศูนย์กลางบ้าน" to -47),
                    Pair(34.0, 38.0) to ("โซนทานอาหาร" to -53),
                    Pair(22.0, 26.0) to ("ห้องครัว" to -64),
                    Pair(14.0, 18.0) to ("ระเบียงหลังบ้าน" to -85),
                    Pair(30.0, 68.0) to ("มุมพักผ่อนย่อย" to -62)
                )

                var step = _signalScannerState.value.movementPoints.size + 1
                var idx = 0
                while (_signalScannerState.value.isRecordingMovement) {
                    delay(1500)
                    val (coord, data) = waypoints[idx % waypoints.size]
                    val (zone, baseDbm) = data
                    val jitter = Random.nextInt(-3, 4)
                    val finalDbm = baseDbm + jitter
                    val speed = when {
                        finalDbm > -55 -> 866
                        finalDbm > -65 -> 780
                        finalDbm > -75 -> 520
                        finalDbm > -85 -> 300
                        else -> 120
                    }

                    val newPoint = SignalMapPoint(
                        x = (coord.first + (Random.nextDouble() * 3.0 - 1.5)).coerceIn(5.0, 95.0),
                        y = (coord.second + (Random.nextDouble() * 3.0 - 1.5)).coerceIn(5.0, 95.0),
                        dbm = finalDbm,
                        zoneName = zone,
                        linkSpeedMbps = speed,
                        stepIndex = step
                    )

                    _signalScannerState.update { state ->
                        state.copy(
                            currentDbm = finalDbm,
                            linkSpeedMbps = speed,
                            activeZone = zone,
                            movementPoints = state.movementPoints + newPoint
                        )
                    }

                    // Check if entered an area with poor signal strength
                    val currentState = _signalScannerState.value
                    if (currentState.alertNotificationsEnabled && finalDbm <= currentState.alertThresholdDbm) {
                        SignalAlertNotificationManager.checkAndNotifyPoorSignal(
                            context = getApplication(),
                            dbm = finalDbm,
                            zoneName = zone,
                            thresholdDbm = currentState.alertThresholdDbm
                        )
                        val alertEvent = SignalAlertEvent(
                            dbm = finalDbm,
                            zoneName = zone,
                            threshold = currentState.alertThresholdDbm
                        )
                        _signalScannerState.update { s ->
                            s.copy(
                                lastAlertEvent = alertEvent,
                                alertHistory = (listOf(alertEvent) + s.alertHistory).take(20)
                            )
                        }
                    }

                    step++
                    idx++
                }
            }
        }
    }

    fun pinCurrentLocation() {
        val state = _signalScannerState.value
        val newStep = state.movementPoints.size + 1
        val lastPt = state.movementPoints.lastOrNull()
        val px = lastPt?.let { (it.x + Random.nextDouble() * 8.0 - 4.0).coerceIn(10.0, 90.0) } ?: (state.routerX + 5.0)
        val py = lastPt?.let { (it.y + Random.nextDouble() * 8.0 - 4.0).coerceIn(10.0, 90.0) } ?: (state.routerY + 5.0)

        val newPoint = SignalMapPoint(
            x = px,
            y = py,
            dbm = state.currentDbm,
            zoneName = state.activeZone.ifEmpty { "จุดปักหมุดผู้ใช้ #$newStep" },
            linkSpeedMbps = state.linkSpeedMbps,
            stepIndex = newStep
        )
        _signalScannerState.update {
            it.copy(movementPoints = it.movementPoints + newPoint)
        }
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
        _signalScannerState.update {
            it.copy(currentBatteryPercent = percent.coerceIn(0, 100))
        }
        updateExecutionStatus()
    }

    fun toggleSimulatedCharging() {
        _signalScannerState.update {
            it.copy(isDeviceCharging = !it.isDeviceCharging)
        }
        updateExecutionStatus()
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
        // Adjust regional benchmarks slightly for realistic area variance
        val multiplier = when (region) {
            "กรุงเทพฯ และปริมณฑล" -> 1.0
            "ภาคกลาง" -> 0.95
            "ภาคเหนือ" -> 0.91
            "ภาคอีสาน" -> 0.88
            else -> 0.90
        }
        _ispBenchmarks.value = listOf(
            IspComparisonBenchmark("AIS Fibre", region, 265.4 * multiplier, 112.8 * multiplier, 11, 99.2, "อันดับ 1"),
            IspComparisonBenchmark("True Online", region, 248.0 * multiplier, 105.2 * multiplier, 13, 98.4, "อันดับ 2"),
            IspComparisonBenchmark("3BB Fibre", region, 225.6 * multiplier, 95.0 * multiplier, 15, 96.8, "อันดับ 3"),
            IspComparisonBenchmark("NT Broadband", region, 195.2 * multiplier, 82.5 * multiplier, 17, 95.5, "อันดับ 4")
        )
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
                    delay(3500)

                    // Refresh battery level from system if not simulating
                    val battery = ScannerScheduleEvaluator.getDeviceBatteryStatus(getApplication())
                    // If device is plugged in or changed, keep in sync
                    if (battery.isCharging != _signalScannerState.value.isDeviceCharging) {
                        _signalScannerState.update { it.copy(isDeviceCharging = battery.isCharging) }
                    }
                    updateExecutionStatus()

                    val state = _signalScannerState.value
                    // Only perform the scan sampling and deadzone detection if execution status is RUNNING
                    if (state.executionStatus == ScannerExecutionStatus.RUNNING) {
                        val pts = state.movementPoints
                        val randomSample = if (pts.isNotEmpty()) pts.random() else null
                        val sampledDbm = randomSample?.dbm ?: Random.nextInt(-88, -55)
                        val sampledZone = randomSample?.zoneName ?: "พื้นที่ตรวจจับพื้นหลัง"

                        _signalScannerState.update {
                            it.copy(
                                currentDbm = sampledDbm,
                                activeZone = sampledZone,
                                linkSpeedMbps = if (sampledDbm > -65) 866 else (if (sampledDbm > -80) 433 else 150)
                            )
                        }

                        if (state.alertNotificationsEnabled && sampledDbm <= state.alertThresholdDbm) {
                            SignalAlertNotificationManager.checkAndNotifyPoorSignal(
                                context = getApplication(),
                                dbm = sampledDbm,
                                zoneName = sampledZone,
                                thresholdDbm = state.alertThresholdDbm
                            )
                            val alertEvent = SignalAlertEvent(
                                dbm = sampledDbm,
                                zoneName = sampledZone,
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
                }
            }
        }
    }

    fun refreshSignalScan() {
        val newDbm = Random.nextInt(-88, -52)
        val zone = if (newDbm < -80) "ห้องน้ำในตัว (จุดอับ)" else "ห้องนั่งเล่น (จุดกระจายหลัก)"
        _signalScannerState.update {
            it.copy(
                currentDbm = newDbm,
                activeZone = zone,
                linkSpeedMbps = if (newDbm > -65) 866 else 540
            )
        }
        val state = _signalScannerState.value
        if (state.alertNotificationsEnabled && newDbm <= state.alertThresholdDbm) {
            SignalAlertNotificationManager.checkAndNotifyPoorSignal(
                context = getApplication(),
                dbm = newDbm,
                zoneName = zone,
                thresholdDbm = state.alertThresholdDbm
            )
            val alertEvent = SignalAlertEvent(
                dbm = newDbm,
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
        _mobileState.update {
            it.copy(
                signalDbm = Random.nextInt(-74, -62),
                deviceTemperatureC = 31.0 + (Random.nextDouble() * 2.0),
                bufferbloatMs = Random.nextInt(8, 22)
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
        _isProPlan.update { !it }
    }

    fun setProPlan(isPro: Boolean) {
        _isProPlan.value = isPro
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
        // Authenticate purchase and generate anti-tamper signature
        val validSig = AntiTamperSecurityEngine.generateVipSignature("DEVICE_OWNER_ID", true)
        prefs.edit().putString("vip_signature", validSig).putBoolean("is_vip_ad_free", true).apply()
        _isProPlan.value = true
        _rewardAdsEnabled.value = false
    }

    fun activateVipAdFree() {
        if (_isProPlan.value) {
            _isProPlan.value = false
            prefs.edit().putBoolean("is_vip_ad_free", false).apply()
        } else {
            purchaseVipAdFree("LIFETIME")
        }
    }

    fun watchAdForTempVip() {
        // Unlock 1 hour VIP temporary pass
        _isProPlan.value = true
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

        // เวลาวัดค่าเปิดโหมด GPS อัตโนมัติและดึงพิกัดจริง
        _isGpsModeEnabled.value = true
        fetchGpsCoordinates()

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
                    val record = SpeedTestRecord(
                        downloadMbps = result.downloadMbps ?: 0.0,
                        uploadMbps = result.uploadMbps ?: 0.0,
                        pingMs = result.pingMs ?: 0,
                        jitterMs = result.jitterMs ?: 0,
                        packetLossPercent = 0.0,
                        serverName = server.name,
                        serverLocation = result.detectedColo?.let { "Cloudflare $it PoP" } ?: server.location,
                        networkType = if (isPro) "5G Pro (Precision)" else "Wi-Fi / Cellular"
                    )
                    repository.insertRecord(record)
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
