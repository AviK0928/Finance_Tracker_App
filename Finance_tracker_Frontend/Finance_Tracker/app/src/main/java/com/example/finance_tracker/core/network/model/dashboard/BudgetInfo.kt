package com.example.finance_tracker.core.network.model.dashboard

import com.example.finance_tracker.core.network.model.budget.BudgetResponseDTO

// Mirrors backend Dashboard/dto/BudgetInfo. No aggregate totals: overlapping budgets would count one expense
// several times; each budget carries its own spent/remaining.
data class BudgetInfo(
    val activeBudgets: List<BudgetResponseDTO>
)
