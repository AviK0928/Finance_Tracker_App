package com.example.finance_tracker.features.reports.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finance_tracker.features.reports.state.ReportState

@Composable
fun MonthlyReportSection(state: ReportState) {
    val report = state.monthlyReport ?: return

    Column(modifier = Modifier.fillMaxWidth()) {
        ReportStatCard(label = "Income", value = report.totalIncome)
        ReportStatCard(label = "Expenses", value = report.totalExpense)
        ReportStatCard(label = "Savings", value = report.netSavings)
    }
}

@Composable
private fun ReportStatCard(label: String, value: java.math.BigDecimal) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Text(text = "₹%.2f".format(value), style = MaterialTheme.typography.headlineSmall)
        }
    }
}