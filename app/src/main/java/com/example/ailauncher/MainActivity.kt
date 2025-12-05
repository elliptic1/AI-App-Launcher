package com.example.ailauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ailauncher.ui.AILauncherTheme
import com.example.ailauncher.ui.ChatScreen
import com.example.ailauncher.ui.LauncherScreen
import com.example.ailauncher.ui.LauncherViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: LauncherViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AILauncherTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val chat by viewModel.chat.collectAsStateWithLifecycle()
                val modelState by viewModel.modelDownloadState.collectAsStateWithLifecycle()
                var showChat by remember { mutableStateOf(false) }
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                LauncherScreen(
                    uiState = uiState,
                    onAppClick = { app -> viewModel.launchApp(app) },
                    onOpenChat = { showChat = true },
                    onActionClick = { action -> viewModel.onActionRequested(action) },
                    modelDownloadState = modelState,
                    onRetryModelDownload = { viewModel.ensureModel() }
                )

                if (showChat) {
                    ModalBottomSheet(
                        onDismissRequest = { showChat = false },
                        sheetState = sheetState,
                        containerColor = Color.Transparent
                    ) {
                        ChatScreen(
                            messages = chat,
                            onSend = { text ->
                                viewModel.sendChat(text)
                            }
                        )
                    }
                }
            }
        }
    }

}
