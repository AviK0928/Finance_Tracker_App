package com.example.finance_tracker.features.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.finance_tracker.R
import com.example.finance_tracker.core.network.model.transaction.TransactionType
import com.example.finance_tracker.core.ui.components.EmptyState
import com.example.finance_tracker.core.ui.components.IconLabelRow
import com.example.finance_tracker.features.dashboard.state.DashboardState

@Composable
fun DashboardTransactionView(state: DashboardState) {
    if (state.recentTransactions.isEmpty()) {
        EmptyState(message = "No recent transactions")
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Recent Transactions:")
            state.recentTransactions.forEach { txn ->
                val label = "${txn.type} (${txn.category})"
                val amount = "₹${txn.amount}"
                val icon = if (txn.type == TransactionType.INCOME) R.drawable.ic_income else R.drawable.ic_expense

                IconLabelRow(icon = icon, label = label, value = amount)
            }
        }
    }
}