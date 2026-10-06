package com.example.finance_tracker.features.budgets.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.core.data.model.DefaultCategories
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.budget.*
import com.example.finance_tracker.core.util.hasMoreThanTwoDecimals
import com.example.finance_tracker.core.util.toMoney
import com.example.finance_tracker.features.budgets.domain.BudgetRepo
import com.example.finance_tracker.features.transactions.domain.TransactionRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepo: BudgetRepo,
    private val transactionRepo: TransactionRepo
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state

    fun onEvent(event: BudgetEvent) {
        when (event) {
            is BudgetEvent.LoadBudgets -> loadBudgets()
            is BudgetEvent.OnTitleChanged -> _state.update { it.copy(formTitle = event.title) }
            is BudgetEvent.OnAmountChanged -> _state.update { it.copy(formAmount = event.amount) }
            is BudgetEvent.OnCategoryChanged -> _state.update { it.copy(formCategory = event.category) }
            is BudgetEvent.OnStartDateChanged -> _state.update { it.copy(formStartDate = event.date) }
            is BudgetEvent.OnEndDateChanged -> _state.update { it.copy(formEndDate = event.date) }
            is BudgetEvent.OnFrequencyChanged -> _state.update { it.copy(formFrequency = event.frequency) }
            is BudgetEvent.OnStatusChanged -> _state.update { it.copy(formStatus = event.status) }

            is BudgetEvent.SubmitForm -> submitForm()
            is BudgetEvent.EditBudget -> enterEditMode(event.budgetId)
            is BudgetEvent.DeleteBudget -> deleteBudget(event.budgetId)

            is BudgetEvent.ApplyFilter -> {
                _state.update {
                    it.copy(
                        filterStatus = event.status,
                        filterFrequency = event.frequency
                    )
                }
                loadBudgets()
            }
            is BudgetEvent.ClearFilter -> {
                _state.update {
                    it.copy(
                        filterStatus = null,
                        filterFrequency = null
                    )
                }
                loadBudgets()
            }

            is BudgetEvent.ExportToPdf -> exportPdf()
            is BudgetEvent.ShowForm -> _state.update {
                // The date pickers display today when the field is empty; store it so submit sends it
                val today = LocalDate.now().toString()
                it.copy(
                    isFormVisible = true,
                    formStartDate = it.formStartDate.ifBlank { today },
                    formEndDate = it.formEndDate.ifBlank { today }
                )
            }
            is BudgetEvent.HideForm -> resetForm()
            is BudgetEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
            is BudgetEvent.ClearFormError -> _state.update { it.copy(formError = null) }
            is BudgetEvent.ClearInfo -> _state.update { it.copy(infoMessage = null) }
            is BudgetEvent.PdfSaved -> _state.update {
                it.copy(pendingPdf = null, infoMessage = if (event.saved) "PDF saved" else null)
            }
        }
    }

    /**
     * Pull to refresh: reloads while the current list stays on screen.
     * The screen waits for the returned job, so the indicator stops exactly when the load ends.
     */
    fun refresh(): Job = loadBudgets(showLoading = false)

    private fun loadBudgets(showLoading: Boolean = true): Job {
        loadCategories()
        return viewModelScope.launch {
            if (showLoading) _state.update { it.copy(isLoading = true) }

            when (val result = budgetRepo.getBudgetsByUser()) {
                is NetworkResult.Success -> {
                    _state.update {
                        it.copy(budgets = withFilters(result.data), isOffline = false, isLoading = false)
                    }
                }
                is NetworkResult.Error -> {
                    // Server unreachable: show the last synced copy if there is one
                    val local = if (result.isNoResponse) budgetRepo.getLocalBudgets() else null
                    if (local != null) {
                        _state.update { it.copy(budgets = withFilters(local), isOffline = true, isLoading = false) }
                    } else {
                        _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                    }
                }
                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun withFilters(budgets: List<BudgetResponseDTO>): List<BudgetResponseDTO> = budgets.filter {
        (_state.value.filterStatus == null || it.budgetStatus == _state.value.filterStatus) &&
                (_state.value.filterFrequency == null || it.budgetFrequency == _state.value.filterFrequency)
    }

    /** Suggestions for the budget category: defaults plus the categories the user has spent in. */
    private fun loadCategories() {
        viewModelScope.launch {
            val result = transactionRepo.getCategories()
            if (result is NetworkResult.Success) {
                _state.update { it.copy(categories = DefaultCategories.merge(result.data)) }
            }
        }
    }

    private fun submitForm() {
        val current = _state.value
        val amount = current.formAmount.trim().toBigDecimalOrNull()
        val startDate = current.formStartDate.toLocalDateOrNull()
        val endDate = current.formEndDate.toLocalDateOrNull()
        // Say what is wrong; a plain "Invalid input" left the user guessing
        val problem = when {
            current.formTitle.isBlank() -> "Enter a title"
            amount == null || amount.signum() <= 0 -> "Enter an amount greater than 0"
            amount != null && amount.hasMoreThanTwoDecimals() -> "Amount must have at most 2 decimal places"
            startDate == null || endDate == null -> "Choose start and end dates"
            startDate != null && endDate != null && endDate.isBefore(startDate) -> "End date must be on or after start date"
            else -> null
        }
        if (problem != null || amount == null || startDate == null || endDate == null) {
            _state.update { it.copy(formError = problem) }
            return
        }
        // Category is optional on the backend: blank means the budget covers all expense categories
        val category = current.formCategory.ifBlank { null }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, formError = null) }

            val result = if (current.isEditing && current.selectedBudget != null) {
                budgetRepo.updateBudget(
                    id = current.selectedBudget.id,
                    dto = BudgetUpdateDTO(
                        name = current.formTitle,
                        amount = amount.toMoney(),
                        category = category,
                        startDate = startDate,
                        endDate = endDate,
                        frequency = current.formFrequency,
                        status = current.formStatus
                    )
                )
            } else {
                budgetRepo.createBudget(
                    dto = BudgetCreateDTO(
                        name = current.formTitle,
                        amount = amount.toMoney(),
                        category = category,
                        startDate = startDate,
                        endDate = endDate,
                        frequency = current.formFrequency
                    )
                )
            }

            when (result) {
                is NetworkResult.Success -> {
                    loadBudgets()
                    resetForm()
                    _state.update { it.copy(isLoading = false) }
                }
                // Keep the dialog open with what was typed, and show the reason inside it
                is NetworkResult.Error -> _state.update { it.copy(formError = result.message, isLoading = false) }
                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun enterEditMode(budgetId: Long) {
        val budget = _state.value.budgets.find { it.id == budgetId } ?: return
        _state.update {
            it.copy(
                selectedBudget = budget,
                formTitle = budget.name,
                formAmount = budget.amount.toPlainString(),
                formCategory = budget.category ?: "",
                formStartDate = budget.startDate.toString(),
                formEndDate = budget.endDate.toString(),
                formFrequency = budget.budgetFrequency,
                formStatus = budget.budgetStatus,
                isEditing = true,
                isFormVisible = true
            )
        }
    }

    private fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = budgetRepo.deleteBudget(budgetId)) {
                is NetworkResult.Success -> loadBudgets()
                is NetworkResult.Error -> _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun resetForm() {
        _state.update {
            it.copy(
                formTitle = "",
                formAmount = "",
                formCategory = "",
                formStartDate = "",
                formEndDate = "",
                formFrequency = BudgetFrequency.MONTHLY,
                formStatus = BudgetStatus.ACTIVE,
                formError = null,
                isFormVisible = false,
                isEditing = false,
                selectedBudget = null
            )
        }
    }

    private fun exportPdf() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = budgetRepo.exportBudgetsAsPdf(
                status = _state.value.filterStatus,
                frequency = _state.value.filterFrequency
            )
            when (result) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(isLoading = false, pendingPdf = result.data) }
                }
                is NetworkResult.Error -> _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    // Form dates are ISO "yyyy-MM-dd" strings produced by DatePickerField (LocalDate.toString())
    private fun String.toLocalDateOrNull(): LocalDate? = try {
        LocalDate.parse(this)
    } catch (e: DateTimeParseException) {
        null
    }
}