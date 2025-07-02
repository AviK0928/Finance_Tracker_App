package com.example.finance_tracker.features.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.finance_tracker.R
import com.example.finance_tracker.core.ui.components.IconLabelRow
import com.example.finance_tracker.features.dashboard.state.DashboardState

@Composable
fun DashboardSummaryView(state: DashboardState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IconLabelRow(icon = R.drawable.ic_income, label = "Total Income", value = "₹${state.totalIncome}")
        IconLabelRow(icon = R.drawable.ic_expense, label = "Total Expense", value = "₹${state.totalExpense}")
        IconLabelRow(icon = R.drawable.ic_savings, label = "Net Savings", value = "₹${state.netSavings}")
    }
}