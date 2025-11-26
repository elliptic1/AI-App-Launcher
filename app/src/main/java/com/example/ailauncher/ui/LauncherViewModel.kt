package com.example.ailauncher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ailauncher.ai.AiEngine
import com.example.ailauncher.ai.LocalAiEngine
import com.example.ailauncher.data.AppRepository
import com.example.ailauncher.data.UsageRepository
import com.example.ailauncher.model.AppInfo
import com.example.ailauncher.model.ChatMessage
import com.example.ailauncher.model.ChatRole
import com.example.ailauncher.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val appRepository = AppRepository(application)
    private val usageRepository = UsageRepository(application)
    private val aiEngine: AiEngine = LocalAiEngine(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _chat = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chat: StateFlow<List<ChatMessage>> = _chat.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _apps.value = appRepository.listLaunchableApps()
            val context = usageRepository.buildContextSnapshot(_apps.value)
            _uiState.value = aiEngine.generateUiState(context)
        }
    }

    fun launchApp(appInfo: AppInfo) {
        appRepository.launchApp(appInfo)
    }

    fun sendChat(message: String) {
        viewModelScope.launch {
            val updatedHistory = _chat.value + ChatMessage(ChatRole.USER, message)
            _chat.value = updatedHistory
            val aiReply = aiEngine.respondToChat(updatedHistory)
            _chat.update { it + aiReply }
        }
    }
}
