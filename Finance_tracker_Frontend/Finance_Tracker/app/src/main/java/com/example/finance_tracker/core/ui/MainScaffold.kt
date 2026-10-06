package com.example.finance_tracker.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.finance_tracker.R
import com.example.finance_tracker.core.navigation.BottomNavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    showTopBar: Boolean = true,
    showBottomBar: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text(getTitleForRoute(currentRoute)) },
                    actions = {
                        IconButton(onClick = { onNavigate(com.example.finance_tracker.core.navigation.Route.NOTIFICATIONS) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_notifications),
                                contentDescription = "Notifications"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomAppNavBar(currentRoute = currentRoute, onNavigate = onNavigate)
            }
        },
        content = content
    )
}

@Composable
private fun getTitleForRoute(route: String): String {
    return when (route) {
        com.example.finance_tracker.core.navigation.Route.DASHBOARD -> "Dashboard"
        com.example.finance_tracker.core.navigation.Route.TRANSACTIONS -> "Transactions"
        com.example.finance_tracker.core.navigation.Route.BUDGETS -> "Budgets"
        com.example.finance_tracker.core.navigation.Route.REPORTS -> "Reports"
        com.example.finance_tracker.core.navigation.Route.SETTINGS -> "Settings"
        com.example.finance_tracker.core.navigation.Route.NOTIFICATIONS -> "Notifications"
        else -> "Finance Tracker"
    }
}