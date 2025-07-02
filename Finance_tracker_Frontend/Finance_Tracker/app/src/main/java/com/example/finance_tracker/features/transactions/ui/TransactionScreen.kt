package com.example.finance_tracker.features.transactions.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance_tracker.core.ui.components.ErrorMessage
import com.example.finance_tracker.core.ui.components.LoadingIndicator
import com.example.finance_tracker.features.transactions.state.TransactionEvent
import com.example.finance_tracker.features.transactions.state.TransactionViewModel

@Composable
fun TransactionsScreen(
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> LoadingIndicator()
            state.errorMessage != null -> ErrorMessage(
                message = state.errorMessage ?: "Unknown error",
                onDismiss = { viewModel.onEvent(TransactionEvent.ClearError) }
            )
            else -> {
                TransactionList(
                    state = state,
                    onLoadNext = { viewModel.onEvent(TransactionEvent.LoadNextPage) },
                    onEdit = { viewModel.onEvent(TransactionEvent.EditTransaction(it)) },
                    onDelete = { viewModel.onEvent(TransactionEvent.DeleteTransaction(it)) }
                )
            }
        }

        FloatingActionButton(
            onClick = { viewModel.onEvent(TransactionEvent.ShowForm) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
        }
    }

    if (state.isFormVisible) {
        TransactionFormDialog(state = state, onEvent = viewModel::onEvent)
    }
}

