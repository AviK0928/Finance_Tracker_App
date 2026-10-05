package com.example.finance_tracker.features.transactions.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
import com.example.finance_tracker.core.ui.components.ErrorMessage
import com.example.finance_tracker.core.ui.components.LoadingIndicator
import com.example.finance_tracker.features.transactions.state.TransactionEvent
import com.example.finance_tracker.features.transactions.state.TransactionViewModel

@Composable
fun TransactionsScreen(
    viewModel: TransactionViewModel = hiltViewModel()
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
        viewModel.onEvent(TransactionEvent.PdfSaved(saved))
    }

    LaunchedEffect(state.pendingPdf) {
        if (state.pendingPdf != null) {
            pdfLauncher.launch("transactions.pdf")
        }
    }

    LaunchedEffect(state.infoMessage) {
        state.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(TransactionEvent.ClearInfo)
        }
    }

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

        // Exports the transactions matching the current filter
        SmallFloatingActionButton(
            onClick = { viewModel.onEvent(TransactionEvent.ExportToPDF) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 88.dp)
        ) {
            Icon(Icons.Default.Download, contentDescription = "Export PDF")
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))

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

