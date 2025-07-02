package com.example.finance_tracker.core.network.model.dashboard

import com.example.finance_tracker.core.network.model.budget.BudgetResponseDTO
import java.math.BigDecimal

data class BudgetInfo(
    val totalBudget: BigDecimal,
    val remainingBudget: BigDecimal,
    val activeBudgets: List<BudgetResponseDTO>
)