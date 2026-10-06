package com.example.finance_tracker.features.transactions.state

import com.example.finance_tracker.core.network.model.transaction.TransactionFilterDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionType

sealed class TransactionEvent {
    object LoadInitial : TransactionEvent()
    object LoadNextPage : TransactionEvent()

    data class ApplyFilter(val filter: TransactionFilterDTO) : TransactionEvent()
    object ClearFilter : TransactionEvent()

    object SubmitForm : TransactionEvent()
    data class EditTransaction(val transactionId: Long) : TransactionEvent()
    data class DeleteTransaction(val transactionId: Long) : TransactionEvent()

    object ShowForm : TransactionEvent()
    object HideForm : TransactionEvent()

    data class OnAmountChanged(val amount: String) : TransactionEvent()
    data class OnCategoryChanged(val category: String) : TransactionEvent()
    data class OnTypeChanged(val type: TransactionType) : TransactionEvent()
    data class OnDateChanged(val date: String) : TransactionEvent()
    data class OnDescriptionChanged(val desc: String) : TransactionEvent()
    object ExportToPDF : TransactionEvent()
    data class PdfSaved(val saved: Boolean) : TransactionEvent()
    object ClearInfo : TransactionEvent()
    object ClearError : TransactionEvent()
}