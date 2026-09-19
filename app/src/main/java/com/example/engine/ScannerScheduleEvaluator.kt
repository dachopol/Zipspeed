package com.example.engine

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.model.ScannerExecutionStatus
import com.example.model.ScannerScheduleConfig
import com.example.model.SchedulePreset
import java.util.Calendar

object ScannerScheduleEvaluator {

    data class BatteryStatus(
        val levelPercent: Int,
        val isCharging: Boolean
    )

    /**
     * Reads the real Android battery level and charging state using BatteryManager.
     */
    fun getDeviceBatteryStatus(context: Context): BatteryStatus {
        return try {
            val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, batteryFilter)

            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

            val batteryPct = if (level >= 0 && scale > 0) {
                ((level.toFloat() / scale.toFloat()) * 100).toInt()
            } else {
                80
            }

            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            BatteryStatus(
                levelPercent = batteryPct.coerceIn(0, 100),
                isCharging = isCharging
            )
        } catch (e: Exception) {
            BatteryStatus(levelPercent = 80, isCharging = false)
        }
    }

    /**
     * Evaluates whether the current local time falls within the configured schedule window.
     */
    fun isWithinTimeWindow(
        config: ScannerScheduleConfig,
        currentHour: Int,
        currentMinute: Int
    ): Boolean {
        if (!config.scheduleEnabled || config.preset == SchedulePreset.ALL_DAY) {
            return true
        }

        val currentTotalMinutes = currentHour * 60 + currentMinute
        val (effectiveStartH, effectiveStartM, effectiveEndH, effectiveEndM) = when (config.preset) {
            SchedulePreset.DAYTIME -> listOf(8, 0, 22, 0)
            SchedulePreset.WORK_HOURS -> listOf(9, 0, 18, 0)
            SchedulePreset.NIGHT_SHIFT -> listOf(22, 0, 6, 0)
            SchedulePreset.CUSTOM -> listOf(config.startHour, config.startMinute, config.endHour, config.endMinute)
            SchedulePreset.ALL_DAY -> listOf(0, 0, 23, 59)
        }

        val startTotalMinutes = effectiveStartH * 60 + effectiveStartM
        val endTotalMinutes = effectiveEndH * 60 + effectiveEndM

        return if (startTotalMinutes <= endTotalMinutes) {
            currentTotalMinutes in startTotalMinutes until endTotalMinutes
        } else {
            // Overnight window, e.g., 22:00 -> 06:00
            currentTotalMinutes >= startTotalMinutes || currentTotalMinutes < endTotalMinutes
        }
    }

    /**
     * Checks if current battery is above the minimum threshold or charging bypass applies.
     */
    fun isBatterySufficient(
        config: ScannerScheduleConfig,
        batteryPercent: Int,
        isCharging: Boolean
    ): Boolean {
        return if (isCharging && config.bypassBatteryWhenCharging) {
            true
        } else {
            batteryPercent >= config.minBatteryThresholdPercent
        }
    }

    /**
     * Determines the execution status of the background scanner based on:
     * - Background scan enabled switch
     * - Time schedule window
     * - Battery level threshold and charging bypass
     */
    fun evaluateStatus(
        backgroundScanEnabled: Boolean,
        config: ScannerScheduleConfig,
        batteryPercent: Int,
        isCharging: Boolean,
        calendar: Calendar = Calendar.getInstance()
    ): ScannerExecutionStatus {
        if (!backgroundScanEnabled) {
            return ScannerExecutionStatus.DISABLED
        }

        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val withinSchedule = isWithinTimeWindow(config, currentHour, currentMinute)
        if (!withinSchedule) {
            return ScannerExecutionStatus.PAUSED_SCHEDULE
        }

        if (!isBatterySufficient(config, batteryPercent, isCharging)) {
            return ScannerExecutionStatus.PAUSED_BATTERY
        }

        return ScannerExecutionStatus.RUNNING
    }
}
