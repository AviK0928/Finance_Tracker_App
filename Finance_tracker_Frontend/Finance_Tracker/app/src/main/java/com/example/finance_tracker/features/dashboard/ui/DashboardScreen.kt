package com.example.finance_tracker.features.dashboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance_tracker.features.dashboard.state.*
import com.example.finance_tracker.core.ui.components.EmptyState
import com.example.finance_tracker.core.ui.components.ErrorSnackbarEffect
import com.example.finance_tracker.core.ui.components.LoadingIndicator
import com.example.finance_tracker.core.ui.components.OfflineBanner
import com.example.finance_tracker.core.ui.components.RefreshableBox

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.onEvent(DashboardEvent.LoadDashboardData)
    }

    ErrorSnackbarEffect(state.error, snackbarHostState) { viewModel.onEvent(DashboardEvent.ClearError) }

    Box(modifier = Modifier.fillMaxSize()) {
        RefreshableBox(onRefresh = { viewModel.refresh().join() }, modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                DashboardTabSelector(
                    selectedView = state.currentView,
                    onViewSelected = { viewModel.onEvent(DashboardEvent.ChangeView(it)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isOffline) OfflineBanner()

                when {
                    state.isLoading -> LoadingIndicator()
                    // Nothing loaded yet and no offline copy: zeros would look like real totals
                    state.loadFailed -> EmptyState(message = "Could not load the dashboard. Pull down to try again.")
                    else -> when (state.currentView) {
                        DashboardView.SUMMARY -> DashboardSummaryView(state)
                        DashboardView.BUDGET -> DashboardBudgetView(state)
                        DashboardView.TRANSACTIONS -> DashboardTransactionView(state)
                    }
                }
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
