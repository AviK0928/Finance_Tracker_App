package com.example.finance_tracker.core.network.model.sync

import com.example.finance_tracker.core.network.model.settings.UserSettingDTO

data class SyncResponseDTO(
    val budgets: List<BudgetDTO>,
    val transactions: List<TransactionDTO>,
    val metadata: SyncMetadataDTO,
    val settings: List<UserSettingDTO>,
    val largeSync: Boolean
)