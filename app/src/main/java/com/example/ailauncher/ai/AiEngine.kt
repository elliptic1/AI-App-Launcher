package com.example.ailauncher.ai

import com.example.ailauncher.model.AiContext
import com.example.ailauncher.model.ChatMessage
import com.example.ailauncher.model.LauncherAction
import com.example.ailauncher.model.UiState

interface AiEngine {
    suspend fun generateUiState(context: AiContext): UiState
    suspend fun respondToChat(history: List<ChatMessage>): ChatMessage
    suspend fun suggestActions(context: AiContext): List<LauncherAction>
}
