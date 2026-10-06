package com.example.finance_tracker.features.transactions.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.core.data.model.DefaultCategories
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.transaction.*
import com.example.finance_tracker.core.util.hasMoreThanTwoDecimals
import com.example.finance_tracker.core.util.toMoney
import com.example.finance_tracker.features.transactions.domain.TransactionRepo
import com.example.finance_tracker.core.sync.SyncTrigger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepo: TransactionRepo,
    private val syncTrigger: SyncTrigger
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionState())
    val state: StateFlow<TransactionState> = _state

    fun onEvent(event: TransactionEvent) {
        when (event) {
            is TransactionEvent.LoadInitial -> {
                loadTransactions(0)
                loadCategories()
            }
            is TransactionEvent.LoadNextPage -> loadTransactions(_state.value.currentPage + 1)
            is TransactionEvent.ApplyFilter -> applyFilter(event.filter)
            is TransactionEvent.ClearFilter -> clearFilter()
            is TransactionEvent.SubmitForm -> submitForm()
            is TransactionEvent.EditTransaction -> editTransaction(event.transactionId)
            is TransactionEvent.DeleteTransaction -> deleteTransaction(event.transactionId)
            is TransactionEvent.ShowForm -> _state.update {
                // The date field shows today when empty; store it so submit sends what the user sees
                it.copy(isFormVisible = true, formDate = it.formDate.ifBlank { LocalDateTime.now().withNano(0).toString() })
            }
            is TransactionEvent.HideForm -> resetForm()
            // Editing any field clears the form error: the user is acting on it
            is TransactionEvent.OnAmountChanged -> _state.update { it.copy(formAmount = event.amount, formError = null) }
            is TransactionEvent.OnCategoryChanged -> _state.update { it.copy(formCategory = event.category, formError = null) }
            is TransactionEvent.OnTypeChanged -> _state.update { it.copy(formType = event.type, formError = null) }
            is TransactionEvent.OnDateChanged -> _state.update { it.copy(formDate = event.date, formError = null) }
            is TransactionEvent.OnDescriptionChanged -> _state.update { it.copy(formDescription = event.desc, formError = null) }
            is TransactionEvent.ExportToPDF -> exportToPDF()
            is TransactionEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
            is TransactionEvent.PdfSaved -> _state.update {
                it.copy(pendingPdf = null, infoMessage = if (event.saved) "PDF saved" else null)
            }
            is TransactionEvent.ClearInfo -> _state.update { it.copy(infoMessage = null) }
        }
    }

    /**
     * Pull to refresh: reloads the first page while the current list stays on screen.
     * The screen waits for the returned job, so the indicator stops exactly when the load ends.
     */
    fun refresh(): Job {
        // A pull also refreshes the offline copy (fire and forget; ignored while logged out)
        syncTrigger.requestSync()
        return loadTransactions(0, showLoading = false)
    }

    private fun loadTransactions(page: Int, showLoading: Boolean = true): Job {
        return viewModelScope.launch {
            if (showLoading) _state.update { it.copy(isLoading = true) }

            val result = transactionRepo.getFilteredTransactionsPaginated(
                filter = _state.value.filter,
                page = page,
                size = _state.value.pageSize
            )

            when (result) {
                is NetworkResult.Success -> {
                    _state.update {
                        it.copy(
                            transactions = if (page == 0) result.data.content else it.transactions + result.data.content,
                            currentPage = result.data.number,
                            totalPages = result.data.totalPages,
                            isOffline = false,
                            isLoading = false
                        )
                    }
                }
                is NetworkResult.Error -> {
                    // Server unreachable: show the last synced copy (all of it, one page) if there is one
                    val local = if (result.isNoResponse) transactionRepo.getLocalTransactions(_state.value.filter) else null
                    if (local != null) {
                        _state.update {
                            it.copy(transactions = local, currentPage = 0, totalPages = 1, isOffline = true, isLoading = false)
                        }
                    } else {
                        _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                    }
                }
                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    /** Suggestions for the category field: defaults plus the user's own categories. */
    private fun loadCategories() {
        viewModelScope.launch {
            val result = transactionRepo.getCategories()
            if (result is NetworkResult.Success) {
                _state.update { it.copy(categories = DefaultCategories.merge(result.data)) }
            }
        }
    }

    private fun applyFilter(filter: TransactionFilterDTO) {
        _state.update { it.copy(filter = filter, currentPage = 0, transactions = emptyList()) }
        loadTransactions(0)
    }

    private fun clearFilter() {
        _state.update { it.copy(filter = TransactionFilterDTO(), currentPage = 0, transactions = emptyList()) }
        loadTransactions(0)
    }

    private fun submitForm() {
        val state = _state.value
        val amount = state.formAmount.trim().toBigDecimalOrNull()
        val date = runCatching {
            LocalDateTime.parse(state.formDate, DateTimeFormatter.ISO_DATE_TIME)
        }.getOrNull()

        // Say what is wrong; a plain "Invalid input" left the user guessing
        val problem = when {
            amount == null || amount.signum() <= 0 -> "Enter an amount greater than 0"
            amount != null && amount.hasMoreThanTwoDecimals() -> "Amount must have at most 2 decimal places"
            state.formCategory.isBlank() -> "Choose a category"
            date == null -> "Choose a date"
            else -> null
        }
        if (problem != null || amount == null || date == null) {
            _state.update { it.copy(formError = problem) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, formError = null) }

            val result = if (state.isEditing && state.selectedTransaction != null) {
                transactionRepo.updateTransaction(
                    id = state.selectedTransaction.id,
                    dto = TransactionUpdateDTO(
                        amount = amount.toMoney(),
                        category = state.formCategory,
                        type = state.formType,
                        transactionDate = date,
                        description = state.formDescription
                    )
                )
            } else {
                transactionRepo.createTransaction(
                    dto = TransactionCreateDTO(
                        amount = amount.toMoney(),
                        category = state.formCategory,
                        type = state.formType,
                        transactionDate = date,
                        description = state.formDescription
                    )
                )
            }

            when (result) {
                is NetworkResult.Success -> {
                    resetForm()
                    loadTransactions(0)
                }
                // Keep the dialog open with what was typed, and show the reason inside it
                is NetworkResult.Error -> _state.update { it.copy(formError = result.message, isLoading = false) }
                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun editTransaction(transactionId: Long) {
        val txn = _state.value.transactions.find { it.id == transactionId } ?: return
        _state.update {
            it.copy(
                selectedTransaction = txn,
                formAmount = txn.amount.toPlainString(),
                formCategory = txn.category,
                formType = txn.type,
                formDate = txn.transactionDate.toString(),
                formDescription = txn.description ?: "",
                isEditing = true,
                isFormVisible = true
            )
        }
    }

    private fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = transactionRepo.deleteTransaction(transactionId)) {
                is NetworkResult.Success -> loadTransactions(0)
                is NetworkResult.Error -> _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun resetForm() {
        _state.update {
            it.copy(
                formAmount = "",
                formCategory = "",
                formType = TransactionType.EXPENSE,
                formDate = "",
                formDescription = "",
                formError = null,
                isEditing = false,
                isFormVisible = false,
                selectedTransaction = null
            )
        }
    }

    private fun exportToPDF() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = transactionRepo.exportFilteredTransactionsToPDF(_state.value.filter)
            when (result) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(isLoading = false, pendingPdf = result.data) }
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }
}