package com.example.finance_tracker.core.navigation

import androidx.navigation.NavHostController

class NavigationActions(private val navController: NavHostController) {

    fun navigateTo(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
        }
    }

    /**
     * Top-level destinations (bottom bar tabs, notifications). Only Dashboard stays underneath, so back
     * from any of them returns to Dashboard and back on Dashboard leaves the app. Each tab's state is
     * saved when leaving it and restored when coming back; tapping the current tab adds nothing.
     */
    fun navigateToTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(Route.DASHBOARD) { saveState = true }
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