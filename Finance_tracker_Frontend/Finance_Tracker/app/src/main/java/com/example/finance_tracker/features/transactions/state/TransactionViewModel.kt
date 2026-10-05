package com.example.finance_tracker.features.transactions.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.transaction.*
import com.example.finance_tracker.features.transactions.domain.TransactionRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepo: TransactionRepo
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionState())
    val state: StateFlow<TransactionState> = _state

    fun onEvent(event: TransactionEvent) {
        when (event) {
            is TransactionEvent.LoadInitial -> loadTransactions(0)
            is TransactionEvent.LoadNextPage -> loadTransactions(_state.value.currentPage + 1)
            is TransactionEvent.ApplyFilter -> applyFilter(event.filter)
            is TransactionEvent.ClearFilter -> clearFilter()
            is TransactionEvent.SubmitForm -> submitForm()
            is TransactionEvent.EditTransaction -> editTransaction(event.transactionId)
            is TransactionEvent.DeleteTransaction -> deleteTransaction(event.transactionId)
            is TransactionEvent.ShowForm -> _state.update { it.copy(isFormVisible = true) }
            is TransactionEvent.HideForm -> resetForm()
            is TransactionEvent.OnAmountChanged -> _state.update { it.copy(formAmount = event.amount) }
            is TransactionEvent.OnCategoryChanged -> _state.update { it.copy(formCategory = event.category) }
            is TransactionEvent.OnTypeChanged -> _state.update { it.copy(formType = event.type) }
            is TransactionEvent.OnDateChanged -> _state.update { it.copy(formDate = event.date) }
            is TransactionEvent.OnDescriptionChanged -> _state.update { it.copy(formDescription = event.desc) }
            is TransactionEvent.ExportToPDF -> exportToPDF()
            is TransactionEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadTransactions(page: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

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
                            isLoading = false
                        )
                    }
                }
                is NetworkResult.Error -> {
                    // Fallback to local transactions only on first page
                    if (page == 0) {
                        val local = transactionRepo.getLocalTransactions()
                        _state.update {
                            it.copy(
                                transactions = local.map {
                                    TransactionResponseDTO(
                                        id = 99999999L,
                                        userId = 99999999L,
                                        amount = it.amount.toDouble(),
                                        category = "NA",
                                        type = TransactionType.EXPENSE,
                                        transactionDate = it.updatedAt,
                                        description = it.description ?: "NA",
                                        createdAt = it.updatedAt,
                                        updatedAt = it.updatedAt
                                    )
                                },
                                isLoading = false,
                                errorMessage = "Loaded from local: ${result.message}"
                            )
                        }
                    } else {
                        _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                    }
                }
                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
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
        val amount = state.formAmount.toDoubleOrNull()
        val date = runCatching {
            LocalDateTime.parse(state.formDate, DateTimeFormatter.ISO_DATE_TIME)
        }.getOrNull()

        if (amount == null || date == null || state.formCategory.isBlank()) {
            _state.update { it.copy(errorMessage = "Invalid input") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = if (state.isEditing && state.selectedTransaction != null) {
                transactionRepo.updateTransaction(
                    id = state.selectedTransaction.id,
                    dto = TransactionUpdateDTO(
                        amount = amount,
                        category = state.formCategory,
                        type = state.formType,
                        transactionDate = date,
                        description = state.formDescription
                    )
                )
            } else {
                transactionRepo.createTransaction(
                    dto = TransactionCreateDTO(
                        amount = amount,
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
                is NetworkResult.Error -> _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun editTransaction(transactionId: Long) {
        val txn = _state.value.transactions.find { it.id == transactionId } ?: return
        _state.update {
            it.copy(
                selectedTransaction = txn,
                formAmount = txn.amount.toString(),
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
                    _state.update {
                        it.copy(
                            isLoading = false,
                            // You can store PDF or trigger UI effects from here
                            // exportedPdf = result.data (optional),
                            // isExportSuccessful = true
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }
}