package com.example.finance_tracker.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.finance_tracker.features.auth.ui.AuthScreen
import com.example.finance_tracker.features.auth.ui.ForgotPasswordScreen
import com.example.finance_tracker.features.auth.ui.ResetPasswordScreen

fun NavGraphBuilder.authGraph(navActions: NavigationActions) {
    composable(Route.AUTH) {
        AuthContainer {
            AuthScreen(
                onLoginSuccess = {
                    navActions.navigateAndPopUp(Route.DASHBOARD, Route.AUTH)
                },
                onForgotPassword = {
                    navActions.navigateTo(Route.FORGOT_PASSWORD)
                }
            )
        }
    }

    composable(Route.FORGOT_PASSWORD) {
        AuthContainer {
            ForgotPasswordScreen(
                onResetNavigate = { navActions.navigateTo(Route.RESET_PASSWORD) },
                onBackToLogin = { navActions.popBack() }
            )
        }
    }

    composable(Route.RESET_PASSWORD) {
        AuthContainer {
            ResetPasswordScreen(
                onBackToLogin = {
                    navActions.navigateAndPopUp(Route.AUTH, Route.FORGOT_PASSWORD)
                }
            )
        }
    }
}

/**
 * Auth screens have no scaffold: keep them clear of the status bar, the gesture area and the keyboard
 * (safeDrawing includes the IME), with side margins, and scrollable when the keyboard leaves little room.
 */
@Composable
private fun AuthContainer(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        content()
    }
}
