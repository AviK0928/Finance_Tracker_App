package com.example.finance_tracker.features.reports.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance_tracker.core.ui.components.ErrorMessage
import com.example.finance_tracker.core.ui.components.LoadingIndicator
import com.example.finance_tracker.features.reports.state.*

enum class ReportViewType(val label: String) {
    MONTHLY("Monthly"),
    CATEGORY("Category"),
    TREND("Trend")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ReportViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(ReportViewType.MONTHLY) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(ReportEvent.LoadMonthlyReport("07", 2025))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                actions = {
                    IconButton(onClick = { /* maybe export in future */ }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Reports")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                ReportViewType.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab.ordinal == index,
                        onClick = {
                            selectedTab = tab
                            when (tab) {
                                ReportViewType.MONTHLY ->
                                    viewModel.onEvent(ReportEvent.LoadMonthlyReport("07", 2025))
                                ReportViewType.CATEGORY ->
                                    viewModel.onEvent(ReportEvent.LoadCategoryReport)
                                ReportViewType.TREND ->
                                    viewModel.onEvent(ReportEvent.LoadTrendReport("6m"))
                            }
                        },
                        text = { Text(tab.label) },
                        interactionSource = remember { MutableInteractionSource() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                state.isLoading -> LoadingIndicator()
                state.errorMessage != null -> ErrorMessage(
                    message = state.errorMessage!!,
                    onDismiss = { viewModel.onEvent(ReportEvent.ClearError) }
                )
                else -> when (selectedTab) {
                    ReportViewType.MONTHLY -> MonthlyReportSection(state)
                    ReportViewType.CATEGORY -> CategoryReportSection(state)
                    ReportViewType.TREND -> TrendReportSection(state)
                }
            }
        }
    }
}