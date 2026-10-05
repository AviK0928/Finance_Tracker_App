package com.example.finance_tracker.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.finance_tracker.core.session.SessionViewModel

private val AUTH_ROUTES = setOf(Route.AUTH, Route.FORGOT_PASSWORD, Route.RESET_PASSWORD)

@Composable
fun AppNavGraph(
    navController: NavHostController,
    navActions: NavigationActions,
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()

    // Draw nothing until the stored token has been read, so the first screen is the right one
    val loggedIn = isLoggedIn ?: return

    // A NavHost start destination must not change after the first composition
    val startDestination = remember { if (loggedIn) Route.DASHBOARD else Route.AUTH }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authGraph(navActions)
        mainGraph(navActions)
    }

    // Token gone (logout, account deleted, 401 from AuthInterceptor): back to login, history cleared
    LaunchedEffect(loggedIn) {
        val route = navController.currentDestination?.route
        if (!loggedIn && route !in AUTH_ROUTES) {
            navActions.clearBackStackAndNavigate(Route.AUTH)
        }
    }
}
