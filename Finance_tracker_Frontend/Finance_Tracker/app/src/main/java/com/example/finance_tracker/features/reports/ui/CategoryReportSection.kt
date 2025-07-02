package com.example.finance_tracker.features.reports.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finance_tracker.features.reports.state.ReportState

@Composable
fun CategoryReportSection(state: ReportState) {
    val categories = state.categoryReport
    if (categories.isEmpty()) {
        Text("No data available", style = MaterialTheme.typography.bodyMedium)
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(item.category, style = MaterialTheme.typography.titleMedium)
                    Text("₹%.2f".format(item.totalSpent), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}