package com.example.finance_tracker.features.budgets.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Export: once the PDF is downloaded, ask the user where to save it (Storage Access Framework)
    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val bytes = state.pendingPdf
        val saved = uri != null && bytes != null && runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } != null
        }.getOrDefault(false)
        viewModel.onEvent(BudgetEvent.PdfSaved(saved))
    }

    LaunchedEffect(state.pendingPdf) {
        if (state.pendingPdf != null) {
            pdfLauncher.launch("budgets.pdf")
        }
    }

    LaunchedEffect(state.infoMessage) {
        state.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(BudgetEvent.ClearInfo)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(BudgetEvent.LoadBudgets)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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