package com.example.ailauncher.ai

import com.example.ailauncher.model.AppInfo
import com.example.ailauncher.model.LauncherAction

/**
 * Events that signal when the on-device AI should re-evaluate the launcher UI or tasks.
 */
sealed class AiUserEvent {
    data class AppLaunched(val app: AppInfo) : AiUserEvent()
    data class ActionInvoked(val action: LauncherAction) : AiUserEvent()
    data class ConversationUpdated(val userMessage: String) : AiUserEvent()
    object ManualRefresh : AiUserEvent()
}
