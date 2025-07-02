package com.example.finance_tracker.features.budgets.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.budget.*
import com.example.finance_tracker.core.network.model.sync.BudgetDTO
import com.example.finance_tracker.features.budgets.domain.BudgetRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepo: BudgetRepo
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
            is BudgetEvent.ShowForm -> _state.update { it.copy(isFormVisible = true) }
            is BudgetEvent.HideForm -> resetForm()
            is BudgetEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Always load Room budgets first
            val localBudgets = budgetRepo.getLocalBudgets()
            val fallbackDisplay = localBudgets.map { it.toFallbackResponse() }
            val localCategories = fallbackDisplay.map { it.category }.distinct()

            _state.update {
                it.copy(
                    budgets = fallbackDisplay,
                    categories = localCategories,
                    isLoading = false
                )
            }

            // Try syncing with backend
            when (val result = budgetRepo.getBudgetsByUser()) {
                is NetworkResult.Success -> {
                    val filtered = result.data.filter {
                        (_state.value.filterStatus == null || it.status == _state.value.filterStatus) &&
                                (_state.value.filterFrequency == null || it.frequency == _state.value.filterFrequency)
                    }

                    val remoteCategories = filtered.map { it.category }.distinct()

                    _state.update {
                        it.copy(
                            budgets = filtered,
                            categories = remoteCategories,
                            isLoading = false
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

    private fun submitForm() {
        val current = _state.value
        val amount = current.formAmount.toDoubleOrNull()
        if (amount == null || current.formTitle.isBlank() || current.formCategory.isBlank()) {
            _state.update { it.copy(errorMessage = "Invalid input") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = if (current.isEditing && current.selectedBudget != null) {
                budgetRepo.updateBudget(
                    id = current.selectedBudget.id,
                    dto = BudgetUpdateDTO(
                        title = current.formTitle,
                        amount = amount,
                        category = current.formCategory,
                        startDate = current.formStartDate,
                        endDate = current.formEndDate,
                        frequency = current.formFrequency,
                        status = current.formStatus
                    )
                )
            } else {
                budgetRepo.createBudget(
                    dto = BudgetCreateDTO(
                        title = current.formTitle,
                        amount = amount,
                        category = current.formCategory,
                        startDate = current.formStartDate,
                        endDate = current.formEndDate,
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
                is NetworkResult.Error -> _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun enterEditMode(budgetId: Long) {
        val budget = _state.value.budgets.find { it.id == budgetId } ?: return
        _state.update {
            it.copy(
                selectedBudget = budget,
                formTitle = budget.title,
                formAmount = budget.amount.toString(),
                formCategory = budget.category,
                formStartDate = budget.startDate,
                formEndDate = budget.endDate,
                formFrequency = budget.frequency,
                formStatus = budget.status,
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
                    _state.update { it.copy(isLoading = false) }
                }
                is NetworkResult.Error -> _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    // Converts offline-only BudgetDTO to fake BudgetResponseDTO for rendering
    private fun BudgetDTO.toFallbackResponse(): BudgetResponseDTO {
        return BudgetResponseDTO(
            id = -1,
            userId = -1,
            title = "NA",
            amount = this.amount.toDoubleOrNull() ?: 99999999.0,
            category = "NA",
            startDate = "NA",
            endDate = "NA",
            frequency = BudgetFrequency.MONTHLY,
            status = BudgetStatus.ACTIVE,
            createdAt = "NA",
            updatedAt = this.updatedAt.toString()
        )
    }

    private fun BigDecimal.toDoubleOrNull(): Double? = try {
        this.toDouble()
    } catch (e: Exception) {
        null
    }
}