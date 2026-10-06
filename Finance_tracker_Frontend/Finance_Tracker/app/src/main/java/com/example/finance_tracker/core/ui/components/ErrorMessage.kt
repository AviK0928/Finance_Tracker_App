package com.example.finance_tracker.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * An error shown inline inside a form (dialogs and the auth screens). It has no dismiss button:
 * the ViewModel clears it as soon as the user edits a field. Screen-level errors use
 * [ErrorSnackbarEffect] instead. The text takes the full width and wraps as a whole.
 */
@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}
