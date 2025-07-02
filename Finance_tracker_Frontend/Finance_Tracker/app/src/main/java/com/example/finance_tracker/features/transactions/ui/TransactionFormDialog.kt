package com.example.finance_tracker.features.transactions.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.finance_tracker.features.transactions.state.TransactionEvent
import com.example.finance_tracker.features.transactions.state.TransactionState

@Composable
fun TransactionFormDialog(
    state: TransactionState,
    onEvent: (TransactionEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onEvent(TransactionEvent.HideForm) },
        confirmButton = {}, // Custom buttons inside content
        title = {
            Text(if (state.isEditing) "Edit Transaction" else "New Transaction")
        },
        text = {
            TransactionFormContent(state = state, onEvent = onEvent)
        }
    )
}
