package com.example.finance_tracker.core.network.model.settings

data class ImportSummaryDTO(
    val budgetsImported: Int,
    val budgetsSkipped: Int,
    val transactionsImported: Int,
    val transactionsSkipped: Int,
    val settingsImported: Int,
    val settingsSkipped: Int
)