package com.example.finance_tracker.features.budgets.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.finance_tracker.features.budgets.state.BudgetEvent
import com.example.finance_tracker.features.budgets.state.BudgetState

@Composable
fun BudgetFormDialog(
    state: BudgetState,
    onEvent: (BudgetEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onEvent(BudgetEvent.HideForm) },
        confirmButton = {}, // buttons inside form content
        title = {
            Text(if (state.isEditing) "Edit Budget" else "New Budget")
        },
        text = {
            BudgetFormContent(state = state, onEvent = onEvent)
        }
    )
}