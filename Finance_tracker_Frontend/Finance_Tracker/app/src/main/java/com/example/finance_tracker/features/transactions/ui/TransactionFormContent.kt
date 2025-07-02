package com.example.finance_tracker.features.transactions.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finance_tracker.core.ui.components.*
import com.example.finance_tracker.features.transactions.state.TransactionEvent
import com.example.finance_tracker.features.transactions.state.TransactionState
import java.time.LocalDate

@Composable
fun TransactionFormContent(
    state: TransactionState,
    onEvent: (TransactionEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AmountInputField(
            value = state.formAmount,
            onValueChange = { onEvent(TransactionEvent.OnAmountChanged(it)) },
            label = "Amount"
        )

        Spacer(modifier = Modifier.height(8.dp))

        CategoryDropdown(
            selectedCategory = state.formCategory,
            onCategorySelected = { onEvent(TransactionEvent.OnCategoryChanged(it)) },
            categories = state.categories // <- Replace with actual list from VM/state
        )

        Spacer(modifier = Modifier.height(8.dp))

        TransactionTypeSelector(
            selectedType = state.formType,
            onTypeSelected = { onEvent(TransactionEvent.OnTypeChanged(it)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        DatePickerField(
            selectedDate = LocalDate.now(), // fallback to today if not parsed
            onDateSelected = { onEvent(TransactionEvent.OnDateChanged(it.toString())) },
            label = "Transaction Date"
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.formDescription,
            onValueChange = { onEvent(TransactionEvent.OnDescriptionChanged(it)) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            SecondaryButton(
                text = "Cancel",
                onClick = { onEvent(TransactionEvent.HideForm) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            PrimaryButton(
                text = if (state.isEditing) "Update" else "Create",
                isLoading = state.isLoading,
                onClick = { onEvent(TransactionEvent.SubmitForm) }
            )
        }
    }
}
