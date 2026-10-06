package com.example.finance_tracker.features.budgets.state

import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.budget.BudgetCreateDTO
import com.example.finance_tracker.core.network.model.budget.BudgetFrequency
import com.example.finance_tracker.core.network.model.budget.BudgetResponseDTO
import com.example.finance_tracker.core.network.model.budget.BudgetStatus
import com.example.finance_tracker.core.network.model.budget.BudgetUpdateDTO
import com.example.finance_tracker.core.network.model.transaction.PaginatedTransactionResponse
import com.example.finance_tracker.core.network.model.transaction.TransactionCreateDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionFilterDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionType
import com.example.finance_tracker.core.network.model.transaction.TransactionUpdateDTO
import com.example.finance_tracker.features.budgets.domain.BudgetRepo
import com.example.finance_tracker.features.transactions.domain.TransactionRepo
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

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelTest {

    private class FakeBudgetRepo : BudgetRepo {
        val created = mutableListOf<BudgetCreateDTO>()
        var listCalls = 0
        var onListCall: () -> Unit = {}
        override suspend fun createBudget(dto: BudgetCreateDTO): NetworkResult<BudgetResponseDTO> {
            created += dto
            return NetworkResult.Error("not needed", 500)
        }
        override suspend fun getBudgetsByUser(): NetworkResult<List<BudgetResponseDTO>> {
            listCalls++
            onListCall()
            return NetworkResult.Success(emptyList())
        }
        override suspend fun updateBudget(id: Long, dto: BudgetUpdateDTO) = unused()
        override suspend fun deleteBudget(id: Long) = unused()
        override suspend fun getBudgetById(id: Long) = unused()
        override suspend fun exportBudgetsAsPdf(status: BudgetStatus?, frequency: BudgetFrequency?) = unused()
        override suspend fun getLocalBudgets() = unused()
        private fun unused(): Nothing = throw UnsupportedOperationException("not used in these tests")
    }

    /** Only category suggestions are read by the budget screen. */
    private class FakeTransactionRepo : TransactionRepo {
        override suspend fun getCategories(): NetworkResult<List<String>> = NetworkResult.Success(emptyList())
        override suspend fun createTransaction(dto: TransactionCreateDTO) = unused()
        override suspend fun getTransactionById(id: Long) = unused()
        override suspend fun getAllTransactionsForUser() = unused()
        override suspend fun getFilteredTransactions(
            category: String?, type: TransactionType?, startDate: String?, endDate: String?,
            minAmount: Double?, maxAmount: Double?
        ) = unused()
        override suspend fun updateTransaction(id: Long, dto: TransactionUpdateDTO) = unused()
        override suspend fun deleteTransaction(id: Long) = unused()
        override suspend fun getLocalTransactions(filter: TransactionFilterDTO) = unused()
        override suspend fun getFilteredTransactionsPaginated(
            filter: TransactionFilterDTO, page: Int, size: Int, sort: List<String>
        ): NetworkResult<PaginatedTransactionResponse> = unused()
        override suspend fun exportFilteredTransactionsToPDF(filter: TransactionFilterDTO) = unused()
        private fun unused(): Nothing = throw UnsupportedOperationException("not used in these tests")
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun endBeforeStart_isNamed_insideTheForm_andNothingIsSent() {
        val repo = FakeBudgetRepo()
        val viewModel = BudgetViewModel(repo, FakeTransactionRepo())

        viewModel.onEvent(BudgetEvent.ShowForm)
        viewModel.onEvent(BudgetEvent.OnTitleChanged("Food"))
        viewModel.onEvent(BudgetEvent.OnAmountChanged("500"))
        viewModel.onEvent(BudgetEvent.OnStartDateChanged("2026-10-31"))
        viewModel.onEvent(BudgetEvent.OnEndDateChanged("2026-10-01"))
        viewModel.onEvent(BudgetEvent.SubmitForm)

        assertEquals("End date must be on or after start date", viewModel.state.value.formError)
        assertNull(viewModel.state.value.errorMessage)
        assertTrue(viewModel.state.value.isFormVisible)
        assertTrue(repo.created.isEmpty())
    }

    @Test
    fun missingTitle_isNamed_insideTheForm() {
        val viewModel = BudgetViewModel(FakeBudgetRepo(), FakeTransactionRepo())

        viewModel.onEvent(BudgetEvent.ShowForm)
        viewModel.onEvent(BudgetEvent.OnAmountChanged("500"))
        viewModel.onEvent(BudgetEvent.SubmitForm)

        assertEquals("Enter a title", viewModel.state.value.formError)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun refresh_reloadsTheBudgets_withoutTheFullScreenSpinner() {
        val repo = FakeBudgetRepo()
        val viewModel = BudgetViewModel(repo, FakeTransactionRepo())
        var loadingDuringRefresh: Boolean? = null
        repo.onListCall = { loadingDuringRefresh = viewModel.state.value.isLoading }

        val job = viewModel.refresh()

        assertTrue(job.isCompleted)
        assertEquals(1, repo.listCalls)
        assertEquals(false, loadingDuringRefresh)
        assertFalse(viewModel.state.value.isLoading)
    }
}
