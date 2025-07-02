package com.example.finance_tracker.core.network.model.dashboard

import java.math.BigDecimal

data class SummaryInfo(
    val totalIncome: BigDecimal,
    val totalExpense: BigDecimal,
    val netSavings: BigDecimal
)
