package com.example.finance_tracker.features.budgets.state

import com.example.finance_tracker.core.data.model.DefaultCategories
import com.example.finance_tracker.core.network.model.budget.BudgetFrequency
import com.example.finance_tracker.core.network.model.budget.BudgetResponseDTO
import com.example.finance_tracker.core.network.model.budget.BudgetStatus

data class BudgetState(
    val budgets: List<BudgetResponseDTO> = emptyList(),
    val selectedBudget: BudgetResponseDTO? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,

    // Exported PDF waiting for the user to pick a save location; cleared once handled
    val pendingPdf: ByteArray? = null,
    val isFormVisible: Boolean = false,

    // Form fields
    val formTitle: String = "",
    val formAmount: String = "",
    val formCategory: String = "",
    val formStartDate: String = "",
    val formEndDate: String = "",
    val categories: List<String> = DefaultCategories.ALL,
    val formFrequency: BudgetFrequency = BudgetFrequency.MONTHLY,
    val formStatus: BudgetStatus = BudgetStatus.ACTIVE,

    // Showing the last synced copy because the server could not be reached
    val isOffline: Boolean = false,

    // Validation or save error of the open form, shown inside the dialog (errorMessage is for the screen)
    val formError: String? = null,

    val isEditing: Boolean = false,
    val filterStatus: BudgetStatus? = null,
    val filterFrequency: BudgetFrequency? = null
)