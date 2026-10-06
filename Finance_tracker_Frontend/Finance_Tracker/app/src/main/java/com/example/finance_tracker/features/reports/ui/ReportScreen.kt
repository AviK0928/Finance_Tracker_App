package com.example.finance_tracker.features.reports.ui

import java.time.LocalDate
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance_tracker.core.ui.components.ErrorSnackbarEffect
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

    // The backend takes the java.time.Month name ("OCTOBER"); "07" was rejected with 400
    val today = LocalDate.now()
    val loadThisMonth = { viewModel.onEvent(ReportEvent.LoadMonthlyReport(today.month.name, today.year)) }

    LaunchedEffect(Unit) {
        loadThisMonth()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    ErrorSnackbarEffect(state.errorMessage, snackbarHostState) { viewModel.onEvent(ReportEvent.ClearError) }

    // MainScaffold draws the only top bar and already keeps this screen clear of the system bars
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                                ReportViewType.MONTHLY -> loadThisMonth()
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
                else -> when (selectedTab) {
                    ReportViewType.MONTHLY -> MonthlyReportSection(state)
                    ReportViewType.CATEGORY -> CategoryReportSection(state)
                    ReportViewType.TREND -> TrendReportSection(state)
                }
            }
        }
    }
}