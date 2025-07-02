package com.example.finance_tracker.features.notification.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finance_tracker.core.network.model.notification.NotificationDTO
import java.time.format.DateTimeFormatter

@Composable
fun NotificationList(
    notifications: List<NotificationDTO>,
    selectedIds: Set<Long>,
    onToggleSelection: (Long) -> Unit,
    onMarkAsRead: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onArchive: (Long) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(notifications, key = { it.id }) { notification ->
            val interactionSource = remember { MutableInteractionSource() }
            val isSelected = selectedIds.contains(notification.id)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        onToggleSelection(notification.id)
                    },
                colors = if (isSelected)
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                else
                    CardDefaults.cardColors()
            ){
                Column(Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(notification.title, style = MaterialTheme.typography.titleMedium)
                        if (!notification.read) {
                            Text("Unread", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(notification.message, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        notification.createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy • HH:mm")),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(
                            onClick = { onMarkAsRead(notification.id) },
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Mark Read")
                        }

                        TextButton(
                            onClick = { onArchive(notification.id) },
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            Icon(Icons.Default.Unarchive, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Archive")
                        }

                        TextButton(
                            onClick = { onDelete(notification.id) },
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}