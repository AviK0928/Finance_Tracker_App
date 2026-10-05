package com.example.finance_tracker.features.settings.data

import com.example.finance_tracker.core.data.local.room.dao.BudgetDao
import com.example.finance_tracker.core.data.local.room.dao.SyncMetadataDao
import com.example.finance_tracker.core.data.local.room.dao.TransactionDao
import com.example.finance_tracker.core.data.local.room.dao.UserSettingDao
import com.example.finance_tracker.core.data.local.room.entity.SyncMetadataEntity
import com.example.finance_tracker.core.data.local.room.mapper.BudgetMapper
import com.example.finance_tracker.core.data.local.room.mapper.TransactionMapper
import com.example.finance_tracker.core.data.local.room.mapper.UserSettingMapper
import com.example.finance_tracker.core.network.ApiResponseHandler
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.apiendpoints.SyncApi
import com.example.finance_tracker.core.network.model.sync.SyncRequestDTO
import com.example.finance_tracker.core.network.model.sync.SyncResponseDTO
import com.example.finance_tracker.features.settings.domain.SyncRepo

class SyncRepoImpl(
    private val api: SyncApi,
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao,
    private val userSettingDao: UserSettingDao,
    private val syncMetadataDao: SyncMetadataDao
) : SyncRepo {

    override suspend fun syncData(request: SyncRequestDTO): NetworkResult<SyncResponseDTO> {
        val result = ApiResponseHandler.handleApi { api.syncData(request) }

        if (result is NetworkResult.Success) {
            val data = result.data

            val budgetEntities = BudgetMapper.fromDTOList(data.budgets)
            budgetDao.insertOrReplaceAll(budgetEntities)
            budgetDao.deleteMissingBudgets(data.budgets.map { it.contentHash }.distinct())

            val transactionEntities = TransactionMapper.fromDTOList(data.transactions)
            transactionDao.insertOrReplaceAll(transactionEntities)
            transactionDao.deleteMissingTransactions(data.transactions.map { it.contentHash }.distinct())

            val settingEntities = UserSettingMapper.fromDTOList(data.settings)
            userSettingDao.insertOrReplaceAll(settingEntities)

            val metadata = SyncMetadataEntity(
                latestBudgetUpdate = data.metadata.latestBudgetUpdate,
                latestTransactionUpdate = data.metadata.latestTransactionUpdate
            )
            syncMetadataDao.insertOrReplace(metadata)
        }
        return result
    }
}