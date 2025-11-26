package com.example.ailauncher.data

import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.BatteryManager
import androidx.core.content.getSystemService
import com.example.ailauncher.model.AiContext
import com.example.ailauncher.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

class UsageRepository(private val context: Context) {
    private val usageStatsManager: UsageStatsManager? = context.getSystemService()

    suspend fun buildContextSnapshot(apps: List<AppInfo>): AiContext = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now()
        val timeOfDay = when (now.hour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..22 -> "evening"
            else -> "night"
        }
        val dayOfWeek = DayOfWeek.from(now).getDisplayName(TextStyle.FULL, Locale.getDefault())
        val topApps = queryRecentApps(apps)
        val batteryLevel = currentBatteryLevel()
        AiContext(timeOfDay, dayOfWeek, topApps, batteryLevel)
    }

    private fun queryRecentApps(apps: List<AppInfo>): List<AppInfo> {
        val stats = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            System.currentTimeMillis() - 1000L * 60 * 60 * 24,
            System.currentTimeMillis()
        )?.sortedByDescending { it.lastTimeUsed } ?: emptyList()
        val ranked = stats.mapNotNull { stat ->
            apps.find { it.packageName == stat.packageName }
        }
        return if (ranked.isNotEmpty()) ranked else apps.take(8)
    }

    private fun currentBatteryLevel(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 50
    }
}
