package com.example.finance_tracker.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        MainScaffold(currentRoute = Route.DASHBOARD, onNavigate = navActions::navigateToTopLevel) { padding ->
            Inset(padding) { DashboardScreen() }
        }
    }

    composable(Route.TRANSACTIONS) {
        MainScaffold(currentRoute = Route.TRANSACTIONS, onNavigate = navActions::navigateToTopLevel) { padding ->
            Inset(padding) { TransactionsScreen() }
        }
    }

    composable(Route.BUDGETS) {
        MainScaffold(currentRoute = Route.BUDGETS, onNavigate = navActions::navigateToTopLevel) { padding ->
            Inset(padding) { BudgetScreen() }
        }
    }

    composable(Route.REPORTS) {
        MainScaffold(currentRoute = Route.REPORTS, onNavigate = navActions::navigateToTopLevel) { padding ->
            Inset(padding) { ReportsScreen() }
        }
    }

    composable(Route.SETTINGS) {
        MainScaffold(currentRoute = Route.SETTINGS, onNavigate = navActions::navigateToTopLevel) { padding ->
            // Logout and account deletion clear the token; AppNavGraph then shows the login screen
            Inset(padding) { SettingsScreen() }
        }
    }

    composable(Route.NOTIFICATIONS) {
        MainScaffold(currentRoute = Route.NOTIFICATIONS, onNavigate = navActions::navigateToTopLevel) { padding ->
            Inset(padding) { NotificationScreen() }
        }
    }
}

/** Keeps a screen between MainScaffold's top bar and bottom bar (the screens used to draw underneath both). */
@Composable
private fun Inset(padding: PaddingValues, content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(padding)) { content() }
}
