package com.example.finance_tracker.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
 * The form is centered vertically. verticalScroll measures its content with unbounded height, so the
 * content box gets the visible height as its minimum and centers the form inside it; when the form is
 * taller (keyboard open) it starts at the top and scrolls as before.
 */
@Composable
internal fun AuthContainer(content: @Composable () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
