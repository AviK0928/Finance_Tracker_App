package com.example.finance_tracker.features.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.finance_tracker.R
import com.example.finance_tracker.core.ui.components.EmptyState
import com.example.finance_tracker.core.ui.components.IconLabelRow
import com.example.finance_tracker.features.dashboard.state.DashboardState

@Composable
fun DashboardBudgetView(state: DashboardState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IconLabelRow(icon = R.drawable.ic_budgets, label = "Total Budget", value = "₹${state.totalBudget}")
        IconLabelRow(icon = R.drawable.ic_remaining, label = "Remaining Budget", value = "₹${state.remainingBudget}")

        if (state.activeBudgets.isEmpty()) {
            EmptyState(message = "No active budgets found")
        } else {
            Text("Active Budgets:")
            state.activeBudgets.forEach { budget ->
                Text("- ${budget.name}: ₹${budget.amount}")
            }
        }
    }
}