package com.example.finance_tracker.core.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

/**
 * Read-only date field that opens the system date picker on tap.
 * The tap target is a box laid over the field: a text field consumes taps itself, so a clickable
 * on the field never fired and the picker could not be opened. The picker is created on each tap,
 * so it starts at the current date and calls the current callback.
 */
@Composable
fun DatePickerField(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    label: String = "Select Date",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentOnDateSelected by rememberUpdatedState(onDateSelected)

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedDate.format(DATE_FORMAT),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth -> currentOnDateSelected(LocalDate.of(year, month + 1, dayOfMonth)) },
                        selectedDate.year,
                        selectedDate.monthValue - 1,
                        selectedDate.dayOfMonth
                    ).show()
                }
        )
    }
}
