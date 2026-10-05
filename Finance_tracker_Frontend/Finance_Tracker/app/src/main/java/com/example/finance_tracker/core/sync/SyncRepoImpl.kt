package com.example.finance_tracker.core.sync

import androidx.room.withTransaction
import com.example.finance_tracker.core.data.local.room.FinanceTrackerDatabase
import com.example.finance_tracker.core.data.local.room.entity.SyncStateEntity
import com.example.finance_tracker.core.data.local.room.mapper.BudgetMapper
import com.example.finance_tracker.core.data.local.room.mapper.TransactionMapper
import com.example.finance_tracker.core.network.ApiResponseHandler
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.apiendpoints.SyncApi
import com.example.finance_tracker.core.network.model.sync.SyncResponseDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SyncRepoImpl(
    private val api: SyncApi,
    private val database: FinanceTrackerDatabase
) : SyncRepo {

    // Two triggers at once (app start and a pull-to-refresh) must not apply responses out of order.
    private val mutex = Mutex()

    override suspend fun sync(): NetworkResult<Unit> = mutex.withLock {
        val cursor = database.syncStateDao().getCursor()
        when (val result = ApiResponseHandler.handleApi { api.sync(cursor) }) {
            is NetworkResult.Success -> {
                store(result.data)
                NetworkResult.Success(Unit)
            }
            is NetworkResult.Error -> result
            NetworkResult.Loading -> NetworkResult.Error("Sync did not complete")
        }
    }

    /** Data and cursor in one transaction: a crash halfway leaves the old data with the old cursor. */
    private suspend fun store(response: SyncResponseDTO) = database.withTransaction {
        val transactions = database.transactionDao()
        if (response.fullSync) {
            transactions.deleteAll()
        }
        transactions.upsertAll(response.transactions.map(TransactionMapper::fromDTO))
        if (response.deletedTransactionIds.isNotEmpty()) {
            transactions.deleteByIds(response.deletedTransactionIds)
        }
        // Always the complete list: spending changes without the budget row changing
        database.budgetDao().deleteAll()
        database.budgetDao().insertAll(response.budgets.map(BudgetMapper::fromDTO))
        database.syncStateDao().save(SyncStateEntity(cursor = response.cursor))
    }

    override suspend fun clearLocalData() = mutex.withLock {
        withContext(Dispatchers.IO) { database.clearAllTables() }
    }
}
