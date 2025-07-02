package com.example.finance_tracker.core.network.model.dashboard


import com.example.finance_tracker.core.network.model.transaction.TransactionResponseDTO

data class TransactionInfo(
    val recentTransactions: List<TransactionResponseDTO>
)