package com.example.finance_tracker.features.budgets.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance_tracker.core.ui.components.ErrorMessage
import com.example.finance_tracker.core.ui.components.LoadingIndicator
import com.example.finance_tracker.features.budgets.state.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(BudgetEvent.LoadBudgets)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets") },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(BudgetEvent.ExportToPdf) },
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(BudgetEvent.ShowForm) },
                interactionSource = remember { MutableInteractionSource() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            BudgetFilterChips(
                selectedStatus = state.filterStatus,
                selectedFrequency = state.filterFrequency,
                onStatusSelected = { viewModel.onEvent(BudgetEvent.ApplyFilter(it, state.filterFrequency)) },
                onFrequencySelected = { viewModel.onEvent(BudgetEvent.ApplyFilter(state.filterStatus, it)) },
                onClearFilter = { viewModel.onEvent(BudgetEvent.ClearFilter) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                state.isLoading -> LoadingIndicator()
                state.errorMessage != null -> ErrorMessage(
                    message = state.errorMessage!!,
                    onDismiss = { viewModel.onEvent(BudgetEvent.ClearError) }
                )
                else -> BudgetList(
                    budgets = state.budgets,
                    onEdit = { viewModel.onEvent(BudgetEvent.EditBudget(it)) },
                    onDelete = { viewModel.onEvent(BudgetEvent.DeleteBudget(it)) }
                )
            }
        }
    }

    if (state.isFormVisible) {
        BudgetFormDialog(state = state, onEvent = viewModel::onEvent)
    }
}