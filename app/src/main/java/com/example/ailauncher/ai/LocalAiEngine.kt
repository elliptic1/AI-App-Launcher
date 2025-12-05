package com.example.ailauncher.ai

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import com.example.ailauncher.model.AiContext
import com.example.ailauncher.model.ChatMessage
import com.example.ailauncher.model.ChatRole
import com.example.ailauncher.model.LauncherAction
import com.example.ailauncher.model.UiBlock
import com.example.ailauncher.model.UiState

class LocalAiEngine(private val appContext: Context) : AiEngine {

    @Volatile
    private var hasModel = false

    override suspend fun generateUiState(context: AiContext): UiState {
        val greeting = when (context.timeOfDay) {
            "morning" -> "Good morning"
            "afternoon" -> "Good afternoon"
            "evening" -> "Good evening"
            else -> "Hello"
        }
        val headline = "$greeting — ${context.dayOfWeek.capitalize()}"
        val highlightedApps = context.topApps.take(8)
        val quickActions = listOf(
            LauncherAction(label = "Open Settings", intentAction = android.provider.Settings.ACTION_SETTINGS),
            LauncherAction(label = "Manage Apps", intentAction = android.provider.Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS)
        )
        val blocks = buildList {
            add(UiBlock.TextBlock(headline))
            add(UiBlock.QuickActionsRow(quickActions))
            add(UiBlock.SpacerBlock(8f))
            add(UiBlock.AppGrid(highlightedApps, columns = if (context.batteryLevel < 30) 3 else 4))
        }
        return UiState(blocks)
    }

    override suspend fun respondToChat(history: List<ChatMessage>): ChatMessage {
        val lastUser = history.lastOrNull { it.role == ChatRole.USER }
        val reply = lastUser?.let {
            "You said: ${it.text}. I can launch apps or adjust quick actions."
        } ?: "Hi, I'm your on-device assistant!"
        return ChatMessage(role = ChatRole.AI, text = reply)
    }

    override suspend fun suggestActions(context: AiContext): List<LauncherAction> {
        val baseActions = listOf(
            LauncherAction(label = "Battery ${context.batteryLevel}%", description = "Check battery"),
            LauncherAction(label = "Top app: ${context.topApps.firstOrNull()?.label ?: "None"}")
        )
        return baseActions + context.topApps.take(3).map {
            LauncherAction(label = "Launch ${it.label}", packageName = it.packageName)
        }
    }

    fun warmUp(modelFile: File) {
        if (hasModel) return
        try {
            FileInputStream(modelFile).use { input ->
                val sampleBytes = ByteArray(8)
                input.read(sampleBytes)
                Log.d("LocalAiEngine", "Loaded model ${modelFile.name} (${modelFile.length()} bytes)")
                hasModel = true
            }
        } catch (e: Exception) {
            Log.e("LocalAiEngine", "Failed to warm up model", e)
        }
    }

    private fun String.capitalize(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
