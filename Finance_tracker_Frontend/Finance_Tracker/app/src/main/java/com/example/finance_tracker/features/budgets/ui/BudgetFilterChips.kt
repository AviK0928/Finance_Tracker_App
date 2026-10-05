package com.example.finance_tracker.features.budgets.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finance_tracker.core.network.model.budget.BudgetFrequency
import com.example.finance_tracker.core.network.model.budget.BudgetStatus

@Composable
fun BudgetFilterChips(
    selectedStatus: BudgetStatus?,
    selectedFrequency: BudgetFrequency?,
    onStatusSelected: (BudgetStatus?) -> Unit,
    onFrequencySelected: (BudgetFrequency?) -> Unit,
    onClearFilter: () -> Unit
) {
    Column {
        Text("Status", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BudgetStatus.entries.forEach { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { onStatusSelected(if (selectedStatus == status) null else status) },
                    label = { Text(status.name) },
                    interactionSource = remember { MutableInteractionSource() }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Frequency", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BudgetFrequency.entries.forEach { freq ->
                FilterChip(
                    selected = selectedFrequency == freq,
                    onClick = { onFrequencySelected(if (selectedFrequency == freq) null else freq) },
                    label = { Text(freq.name) },
                    interactionSource = remember { MutableInteractionSource() }
                )
            }
        }

        if (selectedStatus != null || selectedFrequency != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onClearFilter,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                Text("Clear Filters")
            }
        }
    }
}
