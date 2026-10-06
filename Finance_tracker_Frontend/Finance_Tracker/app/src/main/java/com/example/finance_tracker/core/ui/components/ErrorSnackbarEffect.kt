package com.example.finance_tracker.core.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

/**
 * Shows a screen-level error (a failed load or action) in [hostState] for a few seconds, then calls
 * [onShown] so the ViewModel clears it. The screen keeps its content (and pull to refresh) meanwhile.
 * Errors inside form dialogs stay inline ([ErrorMessage]): a dialog is its own window, so a
 * snackbar would be drawn behind it.
 */
@Composable
fun ErrorSnackbarEffect(
    message: String?,
    hostState: SnackbarHostState,
    onShown: () -> Unit
) {
    val latestOnShown by rememberUpdatedState(onShown)
    LaunchedEffect(message) {
        if (message != null) {
            hostState.showSnackbar(message, duration = SnackbarDuration.Short)
            latestOnShown()
        }
    }
}
