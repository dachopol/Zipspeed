package com.example.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

object SignalAlertNotificationManager {

    const val CHANNEL_ID = "zipspeed_signal_alerts"
    const val CHANNEL_NAME = "Signal Strength & Deadzone Alerts"
    const val NOTIFICATION_ID = 2024

    private var lastAlertTimestamp: Long = 0
    private var lastAlertZone: String = ""
    private const val ALERT_COOLDOWN_MS = 20_000L // 20 seconds cooldown per zone

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "เตือนเมื่อผู้ใช้อยู่ในหรือเดินเข้าสู่พื้นที่จุดอับสัญญาณจากข้อมูล Heatmap"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                enableLights(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun canSendNotification(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Dispatches a notification if the signal strength drops below the defined threshold.
     * Incorporates cooldown logic to avoid notification spam while staying in the same deadzone.
     */
    fun checkAndNotifyPoorSignal(
        context: Context,
        dbm: Int,
        zoneName: String,
        thresholdDbm: Int,
        forceTest: Boolean = false
    ): Boolean {
        if (dbm > thresholdDbm && !forceTest) {
            return false
        }

        val now = System.currentTimeMillis()
        val isDifferentZone = zoneName != lastAlertZone
        val cooldownPassed = (now - lastAlertTimestamp) > ALERT_COOLDOWN_MS

        if (!forceTest && !isDifferentZone && !cooldownPassed) {
            return false
        }

        lastAlertTimestamp = now
        lastAlertZone = zoneName

        createNotificationChannel(context)

        if (!canSendNotification(context)) {
            // Cannot post system notification without permission, but internal event is logged
            return false
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_diagnostics", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val displayZone = if (zoneName.isNotBlank()) zoneName else "บริเวณจุดปัจจุบัน"

        val title = "เข้าสู่พื้นที่จุดอับสัญญาณ! ($displayZone)"
        val shortText = "สัญญาณลดลงเหลือ $dbm dBm (ต่ำกว่าเกณฑ์ $thresholdDbm dBm)"
        val expandedText = "ตรวจพบความแรงสัญญาณลดลงผิดปกติที่ $dbm dBm ใน $displayZone ซึ่งตรงกับจุดอับสัญญาณตามข้อมูล Heatmap แนะนำเข้าใกล้เราเตอร์หรือเชื่อมต่อจุด Mesh Wi-Fi"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 150, 300))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
        return true
    }
}
