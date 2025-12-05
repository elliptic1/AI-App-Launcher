package com.example.ailauncher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ailauncher.ai.AiUiOrchestrator
import com.example.ailauncher.ai.AiUserEvent
import com.example.ailauncher.ai.AiEngine
import com.example.ailauncher.ai.LocalAiEngine
import com.example.ailauncher.data.AppRepository
import com.example.ailauncher.data.ModelDownloader
import com.example.ailauncher.data.UsageRepository
import com.example.ailauncher.model.AppInfo
import com.example.ailauncher.model.ChatMessage
import com.example.ailauncher.model.LauncherAction
import com.example.ailauncher.model.ModelDownloadState
import com.example.ailauncher.model.UiState
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val appRepository = AppRepository(application)
    private val usageRepository = UsageRepository(application)
    private val aiEngine: AiEngine = LocalAiEngine(application)
    private val modelDownloader = ModelDownloader(application)

    private val orchestrator = AiUiOrchestrator(appRepository, usageRepository, aiEngine, viewModelScope)

    val uiState: StateFlow<UiState> = orchestrator.uiState

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    val chat: StateFlow<List<ChatMessage>> = orchestrator.chat

    private val _modelDownloadState = MutableStateFlow<ModelDownloadState>(ModelDownloadState.Idle)
    val modelDownloadState: StateFlow<ModelDownloadState> = _modelDownloadState.asStateFlow()

    init {
        refresh()
        ensureModel()
    }

    fun refresh() {
        viewModelScope.launch {
            _apps.value = appRepository.listLaunchableApps()
            orchestrator.refreshUiDesign(AiUserEvent.ManualRefresh)
        }
    }

    fun launchApp(appInfo: AppInfo) {
        appRepository.launchApp(appInfo)
        orchestrator.onAppLaunched(AiUserEvent.AppLaunched(appInfo))
    }

    fun sendChat(message: String) {
        orchestrator.onUserMessage(message)
    }

    fun onActionRequested(action: LauncherAction) {
        appRepository.launchAction(action)
        orchestrator.onActionInvoked(AiUserEvent.ActionInvoked(action))
    }

    fun ensureModel() {
        viewModelScope.launch {
            try {
                val alreadyThere = modelDownloader.currentModelFile().exists()
                if (alreadyThere) {
                    _modelDownloadState.value = ModelDownloadState.Ready(modelDownloader.currentModelFile().absolutePath)
                    warmUpEngine(modelDownloader.currentModelFile())
                    return@launch
                }
                _modelDownloadState.value = ModelDownloadState.Downloading(progress = 0, message = "Preparing on-device model…")
                val file = modelDownloader.ensureModelAvailable { progress ->
                    _modelDownloadState.value = progress
                }
                _modelDownloadState.value = ModelDownloadState.Ready(file.absolutePath)
                warmUpEngine(file)
            } catch (e: Exception) {
                _modelDownloadState.value = ModelDownloadState.Error(e.message ?: "Unable to download model")
            }
        }
    }

    private fun warmUpEngine(file: File) {
        (aiEngine as? LocalAiEngine)?.warmUp(file)
    }
}
