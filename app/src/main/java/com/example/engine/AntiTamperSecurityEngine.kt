package com.example.engine

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import java.io.File
import java.security.MessageDigest

data class SecurityThreatReport(
    val isRooted: Boolean = false,
    val isPatcherInstalled: Boolean = false,
    val isDebuggerAttached: Boolean = false,
    val isIntegrityVerified: Boolean = true,
    val isShieldActive: Boolean = true,
    val securityScore: Int = 100,
    val detectedThreats: List<String> = emptyList(),
    val statusSummary: String = "ระบบปลอดภัย 100% • ป้องกันเครื่องมือปลดล็อค"
)

object AntiTamperSecurityEngine {

    private val SUSPICIOUS_PACKAGES = listOf(
        "com.chelpus.lackypatch" to "Lucky Patcher (เครื่องมือแฮก In-App Purchase)",
        "com.dimonvideo.luckypatcher" to "Lucky Patcher Mod",
        "cc.madkite.freedom" to "Freedom Hack Tool",
        "catch_.me_.if_.you_.can_" to "GameGuardian (Memory Modifier)",
        "org.sbtools.gamehack" to "SB Game Hacker",
        "com.creeplay.creehack" to "CreeHack Tool",
        "com.topjohnwu.magisk" to "Magisk Root Manager",
        "me.weishu.kernelsu" to "KernelSU Root"
    )

    private val SU_PATHS = listOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su"
    )

    fun checkRoot(): Boolean {
        // Check test-keys in build tags
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        // Check binary existence
        for (path in SU_PATHS) {
            try {
                if (File(path).exists()) return true
            } catch (_: Exception) {
                // Ignore permission denial
            }
        }
        return false
    }

    fun checkSuspiciousPackages(context: Context): List<String> {
        val detected = mutableListOf<String>()
        val pm = context.packageManager

        for ((pkg, desc) in SUSPICIOUS_PACKAGES) {
            try {
                pm.getPackageInfo(pkg, 0)
                detected.add(desc)
            } catch (_: PackageManager.NameNotFoundException) {
                // Not found, safe
            } catch (_: Exception) {
                // Safe
            }
        }
        return detected
    }

    fun checkDebugger(): Boolean {
        return try {
            Debug.isDebuggerConnected()
        } catch (_: Throwable) {
            false
        }
    }

    fun generateVipSignature(userId: String, isVip: Boolean): String {
        if (!isVip) return "FREE_TIER"
        val raw = "ZipspeedSec_${userId}_VIP_AUTHENTIC_2026_ANTI_CRACK_HASH"
        val bytes = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyVipSignature(userId: String, signature: String): Boolean {
        val expected = generateVipSignature(userId, true)
        return signature == expected
    }

    fun runDeepSecurityScan(context: Context, shieldActive: Boolean): SecurityThreatReport {
        val isRoot = checkRoot()
        val threats = checkSuspiciousPackages(context)
        val isDebug = checkDebugger()

        val allThreats = mutableListOf<String>()
        if (isRoot) allThreats.add("พบการดัดแปลงระบบ Root / SuperUser")
        allThreats.addAll(threats)
        if (isDebug) allThreats.add("พบการตรวจจับผ่าน Debugger / Hook runtime")

        var score = 100
        if (isRoot) score -= 30
        if (threats.isNotEmpty()) score -= (threats.size * 25)
        if (isDebug) score -= 20
        score = score.coerceIn(0, 100)

        val summary = when {
            allThreats.isEmpty() && shieldActive -> "ความปลอดภัยระดับสูงสุด: ป้องกันการปลดล็อคเถื่อน 100%"
            allThreats.isEmpty() && !shieldActive -> "ระบบปกติ (เกราะป้องกันปิดอยู่)"
            else -> "แจ้งเตือนความปลอดภัย: ตรวจพบ ${allThreats.size} รายการต้องสงสัย"
        }

        return SecurityThreatReport(
            isRooted = isRoot,
            isPatcherInstalled = threats.isNotEmpty(),
            isDebuggerAttached = isDebug,
            isIntegrityVerified = allThreats.isEmpty(),
            isShieldActive = shieldActive,
            securityScore = score,
            detectedThreats = allThreats,
            statusSummary = summary
        )
    }
}
