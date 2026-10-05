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
        // Per budget only: budgets can overlap, so a combined total would count one expense several times
        if (state.activeBudgets.isEmpty()) {
            EmptyState(message = "No active budgets found")
        } else {
            state.activeBudgets.forEach { budget ->
                IconLabelRow(
                    icon = R.drawable.ic_budgets,
                    label = budget.name + (budget.category?.let { " ($it)" } ?: ""),
                    value = "₹%.2f of ₹%.2f (%.0f%%)".format(budget.spentAmount, budget.amount, budget.percentageSpent)
                )
            }
        }
    }
}