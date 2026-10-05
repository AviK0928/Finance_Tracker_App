package com.example.finance_tracker.features.budgets.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finance_tracker.core.network.model.budget.BudgetResponseDTO

@Composable
fun BudgetList(
    budgets: List<BudgetResponseDTO>,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(budgets, key = { it.id }) { budget ->
            Card(
                onClick = { onEdit(budget.id) },
                interactionSource = remember { MutableInteractionSource() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(budget.name, style = MaterialTheme.typography.titleMedium)
                    Text("${budget.amount} • ${budget.category ?: "All categories"}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Spent ${budget.spentAmount} (${budget.percentageSpent}%), remaining ${budget.remainingAmount}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text("From ${budget.startDate} to ${budget.endDate}", style = MaterialTheme.typography.bodySmall)
                    Text("Frequency: ${budget.budgetFrequency.name}", style = MaterialTheme.typography.labelSmall)
                    Text("Status: ${budget.budgetStatus.name}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}