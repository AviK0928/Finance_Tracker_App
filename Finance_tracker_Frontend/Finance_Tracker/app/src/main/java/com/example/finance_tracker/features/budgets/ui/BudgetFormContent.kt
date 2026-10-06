package com.example.finance_tracker.features.budgets.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance_tracker.core.network.model.budget.BudgetFrequency
import com.example.finance_tracker.core.network.model.budget.BudgetStatus
import com.example.finance_tracker.core.ui.components.AmountInputField
import com.example.finance_tracker.core.ui.components.CategoryDropdown
import com.example.finance_tracker.core.ui.components.DatePickerField
import com.example.finance_tracker.core.ui.components.ErrorMessage
import com.example.finance_tracker.core.ui.components.LoadingIndicator
import com.example.finance_tracker.core.ui.components.PrimaryButton
import com.example.finance_tracker.core.ui.components.TextFieldWithLabels
import com.example.finance_tracker.features.budgets.state.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun BudgetFormContent(
    state: BudgetState,
    onEvent: (BudgetEvent) -> Unit
) {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val startDate = try {
        LocalDate.parse(state.formStartDate, formatter)
    } catch (e: Exception) {
        LocalDate.now()
    }

    val endDate = try {
        LocalDate.parse(state.formEndDate, formatter)
    } catch (e: Exception) {
        LocalDate.now()
    }
    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        state.formError?.let { error ->
            ErrorMessage(message = error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        TextFieldWithLabels(
            label = "Title",
            value = state.formTitle,
            onValueChange = { onEvent(BudgetEvent.OnTitleChanged(it)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        AmountInputField(
            value = state.formAmount,
            onValueChange = { onEvent(BudgetEvent.OnAmountChanged(it)) },
            label = "Amount"
        )

        Spacer(modifier = Modifier.height(8.dp))

        CategoryDropdown(
            categories = state.categories,
            selectedCategory = state.formCategory,
            onCategorySelected = { onEvent(BudgetEvent.OnCategoryChanged(it)) },
            label = "Category (empty = all categories)"
        )

        Spacer(modifier = Modifier.height(8.dp))

        DatePickerField(
            label = "Start Date",
            selectedDate = startDate,
            onDateSelected = { onEvent(BudgetEvent.OnStartDateChanged(it.toString())) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        DatePickerField(
            label = "End Date",
            selectedDate = endDate,
            onDateSelected = { onEvent(BudgetEvent.OnEndDateChanged(it.toString())) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Frequency", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BudgetFrequency.entries.forEach { freq ->
                FilterChip(
                    selected = state.formFrequency == freq,
                    onClick = { onEvent(BudgetEvent.OnFrequencyChanged(freq)) },
                    label = { Text(freq.name) },
                    interactionSource = remember { MutableInteractionSource() },
                    trailingIcon = null
                )
            }
        }

        // The backend create request has no status (new budgets are ACTIVE); only update sends it
        if (state.isEditing) {
            Spacer(modifier = Modifier.height(12.dp))

            Text("Status", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BudgetStatus.entries.forEach { status ->
                    FilterChip(
                        selected = state.formStatus == status,
                        onClick = { onEvent(BudgetEvent.OnStatusChanged(status)) },
                        label = { Text(status.name) },
                        interactionSource = remember { MutableInteractionSource() },
                        trailingIcon = null
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = if (state.isEditing) "Update Budget" else "Create Budget",
            onClick = { onEvent(BudgetEvent.SubmitForm) },
            isLoading = state.isLoading
        )
    }
}