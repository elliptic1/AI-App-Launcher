package com.example.ailauncher.ai

import com.example.ailauncher.data.AppRepository
import com.example.ailauncher.data.UsageRepository
import com.example.ailauncher.model.ChatMessage
import com.example.ailauncher.model.ChatRole
import com.example.ailauncher.model.LauncherAction
import com.example.ailauncher.model.UiBlock
import com.example.ailauncher.model.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Central coordinator that keeps the AI responsible for producing and updating the launcher UI.
 * It rebuilds UI state whenever the context changes and routes user events back to the AI
 * so it can decide how to adapt the interface or what tasks to run.
 */
class AiUiOrchestrator(
    private val appRepository: AppRepository,
    private val usageRepository: UsageRepository,
    private val aiEngine: AiEngine,
    private val scope: CoroutineScope
) {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _chat = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chat: StateFlow<List<ChatMessage>> = _chat.asStateFlow()

    /**
     * Rebuilds the launcher UI using the most recent device context and signals the trigger that
     * caused the refresh. The AI always owns the design and can change layout or actions as needed.
     */
    fun refreshUiDesign(trigger: AiUserEvent = AiUserEvent.ManualRefresh) {
        scope.launch {
            val apps = appRepository.listLaunchableApps()
            val context = usageRepository.buildContextSnapshot(apps)
            val generatedState = aiEngine.generateUiState(context)
            val aiActions = aiEngine.suggestActions(context)
            _uiState.value = mergeQuickActions(generatedState, aiActions)
        }
    }

    /**
     * Registers a conversational input and gives the AI a chance to both respond and
     * redesign the interface on the spot.
     */
    fun onUserMessage(message: String) {
        scope.launch {
            val updatedHistory = _chat.value + ChatMessage(ChatRole.USER, message)
            _chat.value = updatedHistory
            val aiReply = aiEngine.respondToChat(updatedHistory)
            _chat.update { it + aiReply }
            refreshUiDesign(AiUserEvent.ConversationUpdated(message))
        }
    }

    /**
     * When the user launches an app, inform the AI so it can reprioritize content on the screen.
     */
    fun onAppLaunched(appEvent: AiUserEvent.AppLaunched) {
        refreshUiDesign(appEvent)
    }

    /**
     * When a quick action is invoked the AI gets another opportunity to update UI or surface
     * additional tasks.
     */
    fun onActionInvoked(event: AiUserEvent.ActionInvoked) {
        refreshUiDesign(event)
    }

    private fun mergeQuickActions(generatedState: UiState, aiActions: List<LauncherAction>): UiState {
        if (aiActions.isEmpty()) return generatedState
        val hasActionsAlready = generatedState.blocks.any { it is UiBlock.QuickActionsRow }
        val blocks = if (hasActionsAlready) {
            generatedState.blocks
        } else {
            listOf(UiBlock.QuickActionsRow(aiActions)) + generatedState.blocks
        }
        return generatedState.copy(blocks = blocks)
    }
}
