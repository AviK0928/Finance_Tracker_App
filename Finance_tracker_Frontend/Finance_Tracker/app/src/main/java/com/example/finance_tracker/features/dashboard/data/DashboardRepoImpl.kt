package com.example.finance_tracker.features.dashboard.data

import com.example.finance_tracker.core.data.local.room.dao.BudgetDao
import com.example.finance_tracker.core.data.local.room.dao.TransactionDao
import com.example.finance_tracker.core.data.local.room.mapper.BudgetMapper
import com.example.finance_tracker.core.data.local.room.mapper.TransactionMapper
import com.example.finance_tracker.core.network.ApiResponseHandler
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.apiendpoints.DashboardApi
import com.example.finance_tracker.core.network.model.dashboard.DashboardResponseDTO
import com.example.finance_tracker.core.offline.OfflineData
import com.example.finance_tracker.features.dashboard.domain.DashboardRepo

class DashboardRepoImpl(
    private val dashboardApi: DashboardApi,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) : DashboardRepo {

    override suspend fun getDashboardData(): NetworkResult<DashboardResponseDTO> {
        return ApiResponseHandler.handleApi {
            dashboardApi.getDashboardData()
        }
    }

    override suspend fun getLocalDashboard(): DashboardResponseDTO? {
        val transactions = transactionDao.getAll()
        val budgets = budgetDao.getAll()
        if (transactions.isEmpty() && budgets.isEmpty()) return null
        return OfflineData.dashboard(transactions.map(TransactionMapper::toDTO), budgets.map(BudgetMapper::toDTO))
    }
}