package com.example.ailauncher.model

data class AiContext(
    val timeOfDay: String,
    val dayOfWeek: String,
    val topApps: List<AppInfo>,
    val batteryLevel: Int
)

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: String? = null
)

data class LauncherAction(
    val label: String,
    val description: String? = null,
    val intentAction: String? = null,
    val packageName: String? = null
)

data class ChatMessage(
    val role: ChatRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ChatRole { USER, AI }

sealed class UiBlock {
    data class AppGrid(val apps: List<AppInfo>, val columns: Int = 4) : UiBlock()
    data class AppRow(val title: String, val apps: List<AppInfo>) : UiBlock()
    data class TextBlock(val text: String) : UiBlock()
    data class QuickActionsRow(val actions: List<LauncherAction>) : UiBlock()
    data class SpacerBlock(val heightDp: Float) : UiBlock()
}

data class UiState(
    val blocks: List<UiBlock> = emptyList()
)
