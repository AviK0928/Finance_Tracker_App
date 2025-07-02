package com.example.finance_tracker.features.transactions.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finance_tracker.core.ui.components.LoadingIndicator
import com.example.finance_tracker.features.transactions.state.TransactionState
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember

@Composable
fun TransactionList(
    state: TransactionState,
    onLoadNext: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(state.transactions) { txn ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                onClick = { onEdit(txn.id) },
                interactionSource = remember { MutableInteractionSource() }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = txn.category,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${txn.amount} • ${txn.type.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = txn.transactionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        if (state.currentPage + 1 < state.totalPages) {
            item {
                LaunchedEffect(Unit) { onLoadNext() }
                LoadingIndicator(modifier = Modifier.padding(16.dp))
            }
        }
    }
}