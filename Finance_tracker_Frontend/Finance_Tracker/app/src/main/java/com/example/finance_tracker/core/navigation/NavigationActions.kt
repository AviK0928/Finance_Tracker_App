package com.example.finance_tracker.core.navigation

import androidx.navigation.NavHostController

class NavigationActions(private val navController: NavHostController) {

    fun navigateTo(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun popBack() {
        navController.popBackStack()
    }

    fun navigateAndPopUp(route: String, popUpToRoute: String) {
        navController.navigate(route) {
            popUpTo(popUpToRoute) {
                inclusive = true
            }
        }
    }

    fun clearBackStackAndNavigate(route: String) {
        navController.navigate(route) {
            popUpTo(0) { inclusive = true }
        }
    }
}