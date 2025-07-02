package com.example.finance_tracker.core.network.model.transaction

data class PaginatedTransactionResponse(
    val content: List<TransactionResponseDTO>,
    val totalPages: Int,
    val totalElements: Long,
    val number: Int, // current page number
    val size: Int,   // page size
    val last: Boolean,
    val first: Boolean,
    val empty: Boolean
)