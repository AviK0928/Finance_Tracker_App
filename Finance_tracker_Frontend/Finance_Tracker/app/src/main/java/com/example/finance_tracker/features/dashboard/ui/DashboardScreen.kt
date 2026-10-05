package com.example.finance_tracker.features.dashboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance_tracker.features.dashboard.state.*
import com.example.finance_tracker.core.ui.components.ErrorMessage
import com.example.finance_tracker.core.ui.components.LoadingIndicator
import com.example.finance_tracker.core.ui.components.OfflineBanner

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(DashboardEvent.LoadDashboardData)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        DashboardTabSelector(
            selectedView = state.currentView,
            onViewSelected = { viewModel.onEvent(DashboardEvent.ChangeView(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isOffline) OfflineBanner()

        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorMessage(
                message = state.error!!,
                onDismiss = { viewModel.onEvent(DashboardEvent.ClearError) }
            )
            else -> when (state.currentView) {
                DashboardView.SUMMARY -> DashboardSummaryView(state)
                DashboardView.BUDGET -> DashboardBudgetView(state)
                DashboardView.TRANSACTIONS -> DashboardTransactionView(state)
            }
        }
    }
}