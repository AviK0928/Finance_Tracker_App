package com.example.finance_tracker.features.dashboard.state

import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.dashboard.BudgetInfo
import com.example.finance_tracker.core.network.model.dashboard.DashboardResponseDTO
import com.example.finance_tracker.core.network.model.dashboard.SummaryInfo
import com.example.finance_tracker.core.network.model.dashboard.TransactionInfo
import com.example.finance_tracker.features.dashboard.domain.DashboardRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private class FakeDashboardRepo(
        var remote: NetworkResult<DashboardResponseDTO>,
        private val local: DashboardResponseDTO?
    ) : DashboardRepo {
        var localCalls = 0
        var onRemoteCall: () -> Unit = {}
        override suspend fun getDashboardData(): NetworkResult<DashboardResponseDTO> {
            onRemoteCall()
            return remote
        }
        override suspend fun getLocalDashboard(): DashboardResponseDTO? {
            localCalls++
            return local
        }
    }

    @Before
    fun setUp() {
        // viewModelScope runs on Dispatchers.Main, which does not exist on the JVM
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun noResponse_showsTheLastSyncedNumbers_markedOffline() {
        val repo = FakeDashboardRepo(
            remote = NetworkResult.Error("Network error: timeout", null, IOException("timeout")),
            local = dashboard(income = "500")
        )
        val viewModel = DashboardViewModel(repo)

        viewModel.onEvent(DashboardEvent.LoadDashboardData)

        val state = viewModel.state.value
        assertTrue(state.isOffline)
        assertNull(state.error)
        assertEquals(0, BigDecimal("500").compareTo(state.totalIncome))
    }

    @Test
    fun serverError_isShown_andTheLocalCopyIsNotUsed() {
        val repo = FakeDashboardRepo(remote = NetworkResult.Error("Server error", 500), local = dashboard(income = "500"))
        val viewModel = DashboardViewModel(repo)

        viewModel.onEvent(DashboardEvent.LoadDashboardData)

        assertEquals("Server error", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isOffline)
        assertEquals(0, repo.localCalls)
    }

    @Test
    fun noResponse_andNothingSynced_showsTheError() {
        val repo = FakeDashboardRepo(
            remote = NetworkResult.Error("Network error: timeout", null, IOException("timeout")),
            local = null
        )
        val viewModel = DashboardViewModel(repo)

        viewModel.onEvent(DashboardEvent.LoadDashboardData)

        assertEquals("Network error: timeout", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isOffline)
    }

    @Test
    fun refresh_backOnline_replacesTheOfflineCopy_withoutTheFullScreenSpinner() {
        val repo = FakeDashboardRepo(
            remote = NetworkResult.Error("Network error: timeout", null, IOException("timeout")),
            local = dashboard(income = "500")
        )
        val viewModel = DashboardViewModel(repo)
        viewModel.onEvent(DashboardEvent.LoadDashboardData)
        assertTrue(viewModel.state.value.isOffline)

        repo.remote = NetworkResult.Success(dashboard(income = "700"))
        var loadingDuringRefresh: Boolean? = null
        repo.onRemoteCall = { loadingDuringRefresh = viewModel.state.value.isLoading }
        val job = viewModel.refresh()

        assertTrue(job.isCompleted)
        assertEquals(false, loadingDuringRefresh)
        assertFalse(viewModel.state.value.isOffline)
        assertEquals(0, BigDecimal("700").compareTo(viewModel.state.value.totalIncome))
    }

    private fun dashboard(income: String) = DashboardResponseDTO(
        summary = SummaryInfo(BigDecimal(income), BigDecimal.ZERO, BigDecimal(income)),
        budget = BudgetInfo(emptyList()),
        transactions = TransactionInfo(emptyList())
    )
}
