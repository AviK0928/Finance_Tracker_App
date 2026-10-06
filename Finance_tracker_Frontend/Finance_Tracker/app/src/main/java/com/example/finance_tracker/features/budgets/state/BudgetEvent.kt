package com.example.finance_tracker.features.budgets.state

import com.example.finance_tracker.core.network.model.budget.BudgetFrequency
import com.example.finance_tracker.core.network.model.budget.BudgetStatus

sealed class BudgetEvent {
    object LoadBudgets : BudgetEvent()
    object SubmitForm : BudgetEvent()

    data class OnTitleChanged(val title: String) : BudgetEvent()
    data class OnAmountChanged(val amount: String) : BudgetEvent()
    data class OnCategoryChanged(val category: String) : BudgetEvent()
    data class OnStartDateChanged(val date: String) : BudgetEvent()
    data class OnEndDateChanged(val date: String) : BudgetEvent()
    data class OnFrequencyChanged(val frequency: BudgetFrequency) : BudgetEvent()
    data class OnStatusChanged(val status: BudgetStatus) : BudgetEvent()

    data class EditBudget(val budgetId: Long) : BudgetEvent()
    data class DeleteBudget(val budgetId: Long) : BudgetEvent()

    object ShowForm : BudgetEvent()
    object HideForm : BudgetEvent()
    object ClearError : BudgetEvent()

    data class ApplyFilter(val status: BudgetStatus?, val frequency: BudgetFrequency?) : BudgetEvent()
    object ClearFilter : BudgetEvent()
    object ExportToPdf : BudgetEvent()
    data class PdfSaved(val saved: Boolean) : BudgetEvent()
    object ClearInfo : BudgetEvent()
}