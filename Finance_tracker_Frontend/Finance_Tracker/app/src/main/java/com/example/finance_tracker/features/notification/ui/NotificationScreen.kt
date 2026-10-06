package com.example.finance_tracker.features.notification.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Markunread
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance_tracker.core.ui.components.ErrorMessage
import com.example.finance_tracker.core.ui.components.LoadingIndicator
import com.example.finance_tracker.features.notification.state.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(viewModel: NotificationViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    // MainScaffold draws the only top bar; the notification actions sit in a row at the top instead
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.selectedNotifications.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            viewModel.onEvent(NotificationEvent.DeleteBulk(state.selectedNotifications.toList()))
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
                    }

                    IconButton(
                        onClick = {
                            viewModel.onEvent(NotificationEvent.ArchiveBulk(state.selectedNotifications.toList()))
                        }
                    ) {
                        Icon(Icons.Default.Unarchive, contentDescription = "Archive Selected")
                    }
                } else {
                    IconButton(
                        onClick = { viewModel.onEvent(NotificationEvent.MarkAllAsRead) }
                    ) {
                        Icon(Icons.Default.Markunread, contentDescription = "Mark All Read")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                state.isLoading -> LoadingIndicator()
                state.errorMessage != null -> ErrorMessage(
                    message = state.errorMessage!!,
                    onDismiss = { viewModel.onEvent(NotificationEvent.ClearError) }
                )
                else -> NotificationList(
                    notifications = state.notifications,
                    selectedIds = state.selectedNotifications,
                    onToggleSelection = { viewModel.onEvent(NotificationEvent.ToggleSelection(it)) },
                    onMarkAsRead = { viewModel.onEvent(NotificationEvent.MarkAsRead(it)) },
                    onDelete = { viewModel.onEvent(NotificationEvent.Delete(it)) },
                    onArchive = { viewModel.onEvent(NotificationEvent.Archive(it)) }
                )
            }
        }
    }
}