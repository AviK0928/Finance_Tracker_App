package com.example.finance_tracker.core.network.model.sync

import com.example.finance_tracker.core.network.model.budget.BudgetResponseDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionResponseDTO

// Mirrors backend Sync/dto/SyncResponseDTO (GET /api/sync).
data class SyncResponseDTO(
    val cursor: String,                       // opaque; sent back unchanged on the next sync
    val fullSync: Boolean,                    // true: replace local transactions
    val transactions: List<TransactionResponseDTO>,
    val deletedTransactionIds: List<Long>,
    val budgets: List<BudgetResponseDTO>      // always the complete list
)
