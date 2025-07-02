package com.example.finance_tracker.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.finance_tracker.features.auth.ui.AuthScreen
import com.example.finance_tracker.features.auth.ui.ForgotPasswordScreen
import com.example.finance_tracker.features.auth.ui.ResetPasswordScreen

fun NavGraphBuilder.authGraph(navActions: NavigationActions) {
    composable(Route.AUTH) {
        AuthScreen(
            onLoginSuccess = {
                navActions.navigateAndPopUp(Route.DASHBOARD, Route.AUTH)
            },
            onForgotPassword = {
                navActions.navigateTo(Route.FORGOT_PASSWORD)
            }
        )
    }

    composable(Route.FORGOT_PASSWORD) {
        ForgotPasswordScreen(
            onResetNavigate = { navActions.navigateTo(Route.RESET_PASSWORD) },
            onBackToLogin = { navActions.popBack() }
        )
    }

    composable(Route.RESET_PASSWORD) {
        ResetPasswordScreen(
            onBackToLogin = {
                navActions.navigateAndPopUp(Route.AUTH, Route.FORGOT_PASSWORD)
            }
        )
    }
}
