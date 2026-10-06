package com.example.finance_tracker.features.transactions.state

import com.example.finance_tracker.core.data.model.DefaultCategories
import com.example.finance_tracker.core.network.model.transaction.PaginatedTransactionResponse
import com.example.finance_tracker.core.network.model.transaction.TransactionFilterDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionResponseDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionType

data class TransactionState(
    val transactions: List<TransactionResponseDTO> = emptyList(),
    val selectedTransaction: TransactionResponseDTO? = null,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,

    // Exported PDF waiting for the user to pick a save location; cleared once handled
    val pendingPdf: ByteArray? = null,

    // Pagination
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val pageSize: Int = 10,

    // Filter
    val filter: TransactionFilterDTO = TransactionFilterDTO(),

    // Form Fields
    val formAmount: String = "",
    val formCategory: String = "",
    val formType: TransactionType = TransactionType.EXPENSE,
    val formDate: String = "",
    val formDescription: String = "",
    val categories: List<String> = DefaultCategories.ALL,

    // Showing the last synced copy because the server could not be reached
    val isOffline: Boolean = false,

    // Validation or save error of the open form, shown inside the dialog (errorMessage is for the screen)
    val formError: String? = null,

    val isEditing: Boolean = false,
    val isFormVisible: Boolean = false
)