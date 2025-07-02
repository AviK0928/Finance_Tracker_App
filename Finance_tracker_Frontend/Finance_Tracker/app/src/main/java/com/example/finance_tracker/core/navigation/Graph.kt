package com.example.finance_tracker.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun AppNavGraph(
    navController: NavHostController,
    navActions: NavigationActions
) {
    NavHost(
        navController = navController,
        startDestination = Route.AUTH
    ) {
        authGraph(navActions)
        mainGraph(navActions)
    }
}
