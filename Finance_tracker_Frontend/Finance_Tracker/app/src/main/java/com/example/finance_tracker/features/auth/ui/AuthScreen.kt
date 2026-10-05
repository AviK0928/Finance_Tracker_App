package com.example.finance_tracker.features.auth.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finance_tracker.core.ui.components.*
import com.example.finance_tracker.features.auth.state.AuthEvent
import com.example.finance_tracker.features.auth.state.AuthViewModel

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.isAuthSuccessful) {
        LaunchedEffect(Unit) {
            onLoginSuccess()
        }
    }

    FormSection(title = if (state.isLoginMode) "Login" else "Register") {
        if (state.sessionExpired) {
            ErrorMessage(
                message = "Your session has expired. Please log in again.",
                onDismiss = { viewModel.onEvent(AuthEvent.DismissSessionExpired) }
            )
        }

        if (!state.isLoginMode) {
            TextFieldWithLabels(
                label = "Name",
                value = state.name,
                onValueChange = { viewModel.onEvent(AuthEvent.OnNameChanged(it)) }
            )
        }

        TextFieldWithLabels(
            label = "Email",
            value = state.email,
            onValueChange = { viewModel.onEvent(AuthEvent.OnEmailChanged(it)) }
        )

        TextFieldWithLabels(
            label = "Password",
            value = state.password,
            onValueChange = { viewModel.onEvent(AuthEvent.OnPasswordChanged(it)) },
            isPassword = true
        )

        if (state.errorMessage != null) {
            ErrorMessage(
                message = state.errorMessage!!,
                onDismiss = { viewModel.onEvent(AuthEvent.ClearError) }
            )
        }

        PrimaryButton(
            text = if (state.isLoginMode) "Login" else "Register",
            onClick = { viewModel.onEvent(AuthEvent.Submit) },
            isLoading = state.isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (state.isLoginMode) "Don't have an account? Register" else "Already have an account? Login",
            modifier = Modifier
                .clickable { viewModel.onEvent(AuthEvent.ToggleAuthMode) }
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Medium
        )

        if (state.isLoginMode) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Forgot Password?",
                modifier = Modifier
                    .clickable { onForgotPassword() }
                    .align(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Medium
            )
        }
    }
}