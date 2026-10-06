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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance_tracker.core.ui.components.ErrorSnackbarEffect
import com.example.finance_tracker.core.ui.components.LoadingIndicator
import com.example.finance_tracker.core.ui.components.OfflineBanner
import com.example.finance_tracker.core.ui.components.RefreshableBox
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

    ErrorSnackbarEffect(state.errorMessage, snackbarHostState) { viewModel.onEvent(BudgetEvent.ClearError) }

    LaunchedEffect(state.infoMessage) {
        state.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(BudgetEvent.ClearInfo)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(BudgetEvent.LoadBudgets)
    }

    // MainScaffold draws the only top bar and already keeps this screen clear of the system bars
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallFloatingActionButton(
                    onClick = { viewModel.onEvent(BudgetEvent.ExportToPdf) },
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Export PDF")
                }
                FloatingActionButton(
                    onClick = { viewModel.onEvent(BudgetEvent.ShowForm) },
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Budget")
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
            BudgetFilterChips(
                selectedStatus = state.filterStatus,
                selectedFrequency = state.filterFrequency,
                onStatusSelected = { viewModel.onEvent(BudgetEvent.ApplyFilter(it, state.filterFrequency)) },
                onFrequencySelected = { viewModel.onEvent(BudgetEvent.ApplyFilter(state.filterStatus, it)) },
                onClearFilter = { viewModel.onEvent(BudgetEvent.ClearFilter) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isOffline) OfflineBanner()

            RefreshableBox(onRefresh = { viewModel.refresh().join() }, modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> LoadingIndicator()
                    else -> BudgetList(
                        budgets = state.budgets,
                        onEdit = { viewModel.onEvent(BudgetEvent.EditBudget(it)) },
                        onDelete = { viewModel.onEvent(BudgetEvent.DeleteBudget(it)) }
                    )
                }
            }
        }
    }

    if (state.isFormVisible) {
        BudgetFormDialog(state = state, onEvent = viewModel::onEvent)
    }
}