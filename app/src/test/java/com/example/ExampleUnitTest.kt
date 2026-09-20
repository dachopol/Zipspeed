package com.example

import com.example.engine.ScannerScheduleEvaluator
import com.example.model.ScannerExecutionStatus
import com.example.model.ScannerScheduleConfig
import com.example.model.SchedulePreset
import com.example.model.SignalMapPoint
import com.example.model.SignalScannerState
import com.example.model.DEFAULT_SERVERS
import com.example.model.SpeedTestState
import com.example.model.MobilePerformanceState
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun productionDefaults_doNotInventNetworkMeasurements() {
    val speed = SpeedTestState()
    assertNull(speed.pingMs)
    assertNull(speed.jitterMs)
    assertNull(speed.packetLossPercent)
    assertNull(speed.downloadMbps)
    assertNull(speed.uploadMbps)

    val mobile = MobilePerformanceState()
    assertEquals(0, mobile.signalDbm)
    assertEquals(0, mobile.signalStrengthDbm)
    assertEquals(0.0, mobile.deviceTemperatureC, 0.0)
    assertEquals(0, mobile.bufferbloatMs)
  }

  @Test
  fun defaultServerDirectory_usesTruthfulAnycastEndpoint() {
    assertTrue(DEFAULT_SERVERS.isNotEmpty())
    val server = DEFAULT_SERVERS.first()
    assertTrue(server.isAnycast)
    assertTrue(server.downloadUrl.startsWith("https://"))
    assertTrue(server.uploadUrl.startsWith("https://"))
    assertEquals(0, server.basePingMs)
    assertEquals(0, server.distanceKm)
    assertEquals(0.0, server.latitude, 0.0)
    assertEquals(0.0, server.longitude, 0.0)
  }

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testSignalMapPoint_propertiesAndDefaults() {
    val point = SignalMapPoint(
        x = 45.0,
        y = 55.0,
        dbm = -65,
        zoneName = "Living Room",
        linkSpeedMbps = 866,
        stepIndex = 1
    )
    assertEquals(45.0, point.x, 0.01)
    assertEquals(55.0, point.y, 0.01)
    assertEquals(-65, point.dbm)
    assertEquals("Living Room", point.zoneName)
    assertEquals(866, point.linkSpeedMbps)
    assertEquals(1, point.stepIndex)
    assertNotNull(point.id)
  }

  @Test
  fun testSignalAlertEvent_creationAndProperties() {
    val alert = com.example.model.SignalAlertEvent(
        dbm = -88,
        zoneName = "En-Suite Bathroom",
        threshold = -80
    )
    assertEquals(-88, alert.dbm)
    assertEquals("En-Suite Bathroom", alert.zoneName)
    assertEquals(-80, alert.threshold)
    assertTrue(alert.dbm <= alert.threshold)
    assertNotNull(alert.id)
    assertTrue(alert.timestamp > 0)
  }

  @Test
  fun testSignalScannerState_alertThresholdHandling() {
    val state = SignalScannerState(
        alertNotificationsEnabled = true,
        alertThresholdDbm = -80,
        currentDbm = -85
    )
    assertTrue(state.alertNotificationsEnabled)
    assertEquals(-80, state.alertThresholdDbm)
    assertTrue(state.currentDbm <= state.alertThresholdDbm)
  }

  @Test
  fun testScannerScheduleEvaluator_timeWindowEvaluation() {
    val daytimeConfig = ScannerScheduleConfig(
        scheduleEnabled = true,
        preset = SchedulePreset.DAYTIME,
        startHour = 8,
        endHour = 22
    )

    // During daytime (e.g. 14:00) -> in window
    assertTrue(ScannerScheduleEvaluator.isWithinTimeWindow(daytimeConfig, currentHour = 14, currentMinute = 0))
    // Outside daytime (e.g. 23:00) -> outside window
    assertFalse(ScannerScheduleEvaluator.isWithinTimeWindow(daytimeConfig, currentHour = 23, currentMinute = 0))
    // Before daytime (e.g. 05:00) -> outside window
    assertFalse(ScannerScheduleEvaluator.isWithinTimeWindow(daytimeConfig, currentHour = 5, currentMinute = 0))

    // Wrapping custom window: 22:00 to 06:00
    val nightShiftConfig = ScannerScheduleConfig(
        scheduleEnabled = true,
        preset = SchedulePreset.CUSTOM,
        startHour = 22,
        endHour = 6
    )
    assertTrue(ScannerScheduleEvaluator.isWithinTimeWindow(nightShiftConfig, currentHour = 23, currentMinute = 0))
    assertTrue(ScannerScheduleEvaluator.isWithinTimeWindow(nightShiftConfig, currentHour = 2, currentMinute = 0))
    assertFalse(ScannerScheduleEvaluator.isWithinTimeWindow(nightShiftConfig, currentHour = 12, currentMinute = 0))
  }

  @Test
  fun testScannerScheduleEvaluator_batteryLevelAndBypass() {
    val config = ScannerScheduleConfig(
        minBatteryThresholdPercent = 20,
        bypassBatteryWhenCharging = true
    )

    // Battery above threshold -> sufficient
    assertTrue(ScannerScheduleEvaluator.isBatterySufficient(config, batteryPercent = 50, isCharging = false))

    // Battery below threshold while not charging -> insufficient
    assertFalse(ScannerScheduleEvaluator.isBatterySufficient(config, batteryPercent = 15, isCharging = false))

    // Battery below threshold while charging (bypass enabled) -> sufficient!
    assertTrue(ScannerScheduleEvaluator.isBatterySufficient(config, batteryPercent = 15, isCharging = true))

    // Battery below threshold while charging with bypass disabled -> insufficient
    val noBypassConfig = config.copy(bypassBatteryWhenCharging = false)
    assertFalse(ScannerScheduleEvaluator.isBatterySufficient(noBypassConfig, batteryPercent = 15, isCharging = true))
  }

  @Test
  fun testScannerScheduleEvaluator_executionStatusDetermination() {
    val config = ScannerScheduleConfig(
        scheduleEnabled = true,
        preset = SchedulePreset.DAYTIME,
        startHour = 8,
        endHour = 22,
        minBatteryThresholdPercent = 20,
        bypassBatteryWhenCharging = true
    )

    fun createCalendar(hour: Int): java.util.Calendar {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
        cal.set(java.util.Calendar.MINUTE, 0)
        return cal
    }

    // 1. Background scan disabled -> DISABLED
    assertEquals(
        ScannerExecutionStatus.DISABLED,
        ScannerScheduleEvaluator.evaluateStatus(
            backgroundScanEnabled = false,
            config = config,
            batteryPercent = 80,
            isCharging = false,
            calendar = createCalendar(12)
        )
    )

    // 2. Scan enabled, midday, good battery -> RUNNING
    assertEquals(
        ScannerExecutionStatus.RUNNING,
        ScannerScheduleEvaluator.evaluateStatus(
            backgroundScanEnabled = true,
            config = config,
            batteryPercent = 80,
            isCharging = false,
            calendar = createCalendar(12)
        )
    )

    // 3. Scan enabled, outside daytime (midnight) -> PAUSED_SCHEDULE
    assertEquals(
        ScannerExecutionStatus.PAUSED_SCHEDULE,
        ScannerScheduleEvaluator.evaluateStatus(
            backgroundScanEnabled = true,
            config = config,
            batteryPercent = 80,
            isCharging = false,
            calendar = createCalendar(0)
        )
    )

    // 4. Scan enabled, in daytime, but battery too low (10%) and not charging -> PAUSED_BATTERY
    assertEquals(
        ScannerExecutionStatus.PAUSED_BATTERY,
        ScannerScheduleEvaluator.evaluateStatus(
            backgroundScanEnabled = true,
            config = config,
            batteryPercent = 10,
            isCharging = false,
            calendar = createCalendar(12)
        )
    )

    // 5. Scan enabled, in daytime, low battery (10%) but charging (bypass active) -> RUNNING
    assertEquals(
        ScannerExecutionStatus.RUNNING,
        ScannerScheduleEvaluator.evaluateStatus(
            backgroundScanEnabled = true,
            config = config,
            batteryPercent = 10,
            isCharging = true,
            calendar = createCalendar(12)
        )
    )
  }
}

