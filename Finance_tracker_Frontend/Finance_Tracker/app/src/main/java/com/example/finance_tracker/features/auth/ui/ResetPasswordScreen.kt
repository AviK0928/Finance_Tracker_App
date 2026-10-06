package com.example.finance_tracker.features.auth.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance_tracker.core.ui.components.*
import com.example.finance_tracker.features.auth.state.AuthEvent
import com.example.finance_tracker.features.auth.state.AuthViewModel

@Composable
fun ResetPasswordScreen(
    onBackToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.resetPasswordSuccess) {
        LaunchedEffect(Unit) {
            onBackToLogin()
        }
    }

    FormSection(title = "Reset Password") {
        TextFieldWithLabels(
            label = "Reset Token",
            value = state.resetToken,
            onValueChange = { viewModel.onEvent(AuthEvent.OnResetTokenChanged(it)) }
        )

        TextFieldWithLabels(
            label = "New Password",
            value = state.password,
            onValueChange = { viewModel.onEvent(AuthEvent.OnPasswordChanged(it)) },
            isPassword = true
        )

        if (state.forgotResetMessage != null) {
            ErrorMessage(message = state.forgotResetMessage!!)
        }

        PrimaryButton(
            text = "Reset Password",
            onClick = { viewModel.onEvent(AuthEvent.ResetPasswordSubmit) },
            isLoading = state.isLoading
        )
    }
}