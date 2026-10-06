package com.example.finance_tracker.features.transactions.state

import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.transaction.PaginatedTransactionResponse
import com.example.finance_tracker.core.network.model.transaction.TransactionCreateDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionFilterDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionResponseDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionType
import com.example.finance_tracker.core.network.model.transaction.TransactionUpdateDTO
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
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    /** Records creates; the list endpoints answer with an empty page. */
    private class FakeTransactionRepo(var createResult: NetworkResult<TransactionResponseDTO>? = null) : TransactionRepo {
        val created = mutableListOf<TransactionCreateDTO>()
        val requestedPages = mutableListOf<Int>()
        var onListCall: () -> Unit = {}

        override suspend fun createTransaction(dto: TransactionCreateDTO): NetworkResult<TransactionResponseDTO> {
            created += dto
            return createResult ?: NetworkResult.Success(
                TransactionResponseDTO(1, 1, dto.amount, dto.category, dto.type, dto.transactionDate, dto.description,
                    LocalDateTime.now(), LocalDateTime.now())
            )
        }
        override suspend fun getFilteredTransactionsPaginated(
            filter: TransactionFilterDTO, page: Int, size: Int, sort: List<String>
        ): NetworkResult<PaginatedTransactionResponse> {
            requestedPages += page
            onListCall()
            return NetworkResult.Success(PaginatedTransactionResponse(emptyList(), 0, 0, 0, size, true, true, true))
        }
        override suspend fun getCategories(): NetworkResult<List<String>> = NetworkResult.Success(emptyList())

        override suspend fun getTransactionById(id: Long) = unused()
        override suspend fun getAllTransactionsForUser() = unused()
        override suspend fun getFilteredTransactions(
            category: String?, type: TransactionType?, startDate: String?, endDate: String?,
            minAmount: BigDecimal?, maxAmount: BigDecimal?
        ) = unused()
        override suspend fun updateTransaction(id: Long, dto: TransactionUpdateDTO) = unused()
        override suspend fun deleteTransaction(id: Long) = unused()
        override suspend fun getLocalTransactions(filter: TransactionFilterDTO) = unused()
        override suspend fun exportFilteredTransactionsToPDF(filter: TransactionFilterDTO) = unused()
        private fun unused(): Nothing = throw UnsupportedOperationException("not used in these tests")
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

    private fun fillIn(viewModel: TransactionViewModel) {
        viewModel.onEvent(TransactionEvent.ShowForm)
        viewModel.onEvent(TransactionEvent.OnAmountChanged("100"))
        viewModel.onEvent(TransactionEvent.OnCategoryChanged("Food"))
    }

    @Test
    fun newExpense_withoutTouchingTheDate_isCreatedForToday() {
        val repo = FakeTransactionRepo()
        val viewModel = TransactionViewModel(repo)

        fillIn(viewModel)
        viewModel.onEvent(TransactionEvent.SubmitForm)

        assertEquals(1, repo.created.size)
        assertEquals(LocalDate.now(), repo.created.single().transactionDate.toLocalDate())
        assertFalse(viewModel.state.value.isFormVisible)
        assertNull(viewModel.state.value.formError)
    }

    @Test
    fun missingCategory_isNamed_insideTheForm_andNothingIsSent() {
        val repo = FakeTransactionRepo()
        val viewModel = TransactionViewModel(repo)

        viewModel.onEvent(TransactionEvent.ShowForm)
        viewModel.onEvent(TransactionEvent.OnAmountChanged("100"))
        viewModel.onEvent(TransactionEvent.SubmitForm)

        assertEquals("Choose a category", viewModel.state.value.formError)
        assertNull(viewModel.state.value.errorMessage)
        assertTrue(viewModel.state.value.isFormVisible)
        assertTrue(repo.created.isEmpty())
    }

    @Test
    fun moreThanTwoDecimals_isNamed_insideTheForm_andNothingIsSent() {
        val repo = FakeTransactionRepo()
        val viewModel = TransactionViewModel(repo)

        fillIn(viewModel)
        viewModel.onEvent(TransactionEvent.OnAmountChanged("10.555"))
        viewModel.onEvent(TransactionEvent.SubmitForm)

        assertEquals("Amount must have at most 2 decimal places", viewModel.state.value.formError)
        assertTrue(repo.created.isEmpty())
    }

    @Test
    fun amount_isSentExactly_atTwoDecimals() {
        val repo = FakeTransactionRepo()
        val viewModel = TransactionViewModel(repo)

        fillIn(viewModel)
        // 17 integer digits (the backend maximum): a Double would send 12345678901234568
        viewModel.onEvent(TransactionEvent.OnAmountChanged(" 12345678901234567.8 "))
        viewModel.onEvent(TransactionEvent.SubmitForm)

        assertEquals(BigDecimal("12345678901234567.80"), repo.created.single().amount)
    }

    @Test
    fun editingAField_clearsTheFormError() {
        val viewModel = TransactionViewModel(FakeTransactionRepo())

        viewModel.onEvent(TransactionEvent.ShowForm)
        viewModel.onEvent(TransactionEvent.OnAmountChanged("100"))
        viewModel.onEvent(TransactionEvent.SubmitForm)
        assertEquals("Choose a category", viewModel.state.value.formError)

        viewModel.onEvent(TransactionEvent.OnCategoryChanged("Food"))

        assertNull(viewModel.state.value.formError)
    }

    @Test
    fun serverError_staysInTheOpenForm() {
        val repo = FakeTransactionRepo(createResult = NetworkResult.Error("Amount must have at most 2 decimal places", 400))
        val viewModel = TransactionViewModel(repo)

        fillIn(viewModel)
        viewModel.onEvent(TransactionEvent.SubmitForm)

        assertEquals("Amount must have at most 2 decimal places", viewModel.state.value.formError)
        assertNull(viewModel.state.value.errorMessage)
        assertTrue(viewModel.state.value.isFormVisible)
        assertEquals("100", viewModel.state.value.formAmount)
    }

    @Test
    fun refresh_reloadsTheFirstPage_withoutTheFullScreenSpinner() {
        val repo = FakeTransactionRepo()
        val viewModel = TransactionViewModel(repo)
        var loadingDuringRefresh: Boolean? = null
        repo.onListCall = { loadingDuringRefresh = viewModel.state.value.isLoading }

        val job = viewModel.refresh()

        assertTrue(job.isCompleted)
        assertEquals(listOf(0), repo.requestedPages)
        assertEquals(false, loadingDuringRefresh)
        assertFalse(viewModel.state.value.isLoading)
    }
}
