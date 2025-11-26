package com.example.ailauncher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ailauncher.model.AppInfo
import com.example.ailauncher.model.LauncherAction
import com.example.ailauncher.model.UiBlock
import com.example.ailauncher.model.UiState

@Composable
fun LauncherScreen(
    uiState: UiState,
    onAppClick: (AppInfo) -> Unit,
    onOpenChat: () -> Unit,
    onActionClick: (LauncherAction) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.blocks) { block ->
                when (block) {
                    is UiBlock.TextBlock -> Text(
                        text = block.text,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    is UiBlock.AppGrid -> AppGrid(block.apps, block.columns, onAppClick)
                    is UiBlock.AppRow -> AppRow(block.title, block.apps, onAppClick)
                    is UiBlock.QuickActionsRow -> QuickActions(block.actions, onActionClick)
                    is UiBlock.SpacerBlock -> Spacer(modifier = Modifier.height(block.heightDp.dp))
                }
            }
        }
        FloatingActionButton(
            onClick = onOpenChat,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Chat, contentDescription = "Chat")
        }
    }
}

@Composable
private fun AppGrid(apps: List<AppInfo>, columns: Int, onAppClick: (AppInfo) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        apps.chunked(columns).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { app ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onAppClick(app) }
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(app.label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRow(title: String, apps: List<AppInfo>, onAppClick: (AppInfo) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            apps.forEach { app ->
                Card(
                    modifier = Modifier.clickable { onAppClick(app) }
                ) {
                    Text(app.label, modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickActions(actions: List<LauncherAction>, onActionClick: (LauncherAction) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.forEach { action ->
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onActionClick(action) }
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(action.label, style = MaterialTheme.typography.titleSmall)
                    action.description?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
