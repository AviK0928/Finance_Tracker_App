package com.example.finance_tracker.features.auth.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance_tracker.core.ui.components.*
import com.example.finance_tracker.features.auth.state.AuthEvent
import com.example.finance_tracker.features.auth.state.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onResetNavigate: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.forgotPasswordSuccess) {
        LaunchedEffect(Unit) {
            onResetNavigate()
        }
    }

    FormSection(title = "Forgot Password") {
        TextFieldWithLabels(
            label = "Email",
            value = state.email,
            onValueChange = { viewModel.onEvent(AuthEvent.OnEmailChanged(it)) },
            keyboardType = KeyboardType.Email
        )

        if (state.forgotResetMessage != null) {
            ErrorMessage(message = state.forgotResetMessage!!)
        }

        PrimaryButton(
            text = "Send Reset Token",
            onClick = { viewModel.onEvent(AuthEvent.ForgotPasswordSubmit) },
            isLoading = state.isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        SecondaryButton(
            text = "Back to Login",
            onClick = onBackToLogin
        )
    }
}