package com.example.finance_tracker.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.finance_tracker.core.ui.MainScaffold
import com.example.finance_tracker.features.dashboard.ui.DashboardScreen
import com.example.finance_tracker.features.transactions.ui.TransactionsScreen
import com.example.finance_tracker.features.budgets.ui.BudgetScreen
import com.example.finance_tracker.features.notification.ui.NotificationScreen
import com.example.finance_tracker.features.reports.ui.ReportsScreen
import com.example.finance_tracker.features.settings.ui.SettingsScreen

fun NavGraphBuilder.mainGraph(navActions: NavigationActions) {
    composable(Route.DASHBOARD) {
        MainScaffold(currentRoute = Route.DASHBOARD, onNavigate = navActions::navigateTo) {
            DashboardScreen()
        }
    }

    composable(Route.TRANSACTIONS) {
        MainScaffold(currentRoute = Route.TRANSACTIONS, onNavigate = navActions::navigateTo) {
            TransactionsScreen()
        }
    }

    composable(Route.BUDGETS) {
        MainScaffold(currentRoute = Route.BUDGETS, onNavigate = navActions::navigateTo) {
            BudgetScreen()
        }
    }

    composable(Route.REPORTS) {
        MainScaffold(currentRoute = Route.REPORTS, onNavigate = navActions::navigateTo) {
            ReportsScreen()
        }
    }

    composable(Route.SETTINGS) {
        MainScaffold(currentRoute = Route.SETTINGS, onNavigate = navActions::navigateTo) {
            // Logout and account deletion clear the token; AppNavGraph then shows the login screen
            SettingsScreen()
        }
    }

    composable(Route.NOTIFICATIONS) {
        MainScaffold(currentRoute = Route.NOTIFICATIONS, onNavigate = navActions::navigateTo) {
            NotificationScreen()
        }
    }
}