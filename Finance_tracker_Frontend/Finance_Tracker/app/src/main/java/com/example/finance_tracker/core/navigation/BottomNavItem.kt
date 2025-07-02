package com.example.finance_tracker.core.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.finance_tracker.R

sealed class BottomNavItem(
    val route: String,
    @DrawableRes val icon: Int,
    @StringRes val label: Int
) {
    object Dashboard : BottomNavItem(Route.DASHBOARD, R.drawable.ic_dashboard, R.string.nav_dashboard)
    object Transactions : BottomNavItem(Route.TRANSACTIONS, R.drawable.ic_transactions, R.string.nav_transactions)
    object Budgets : BottomNavItem(Route.BUDGETS, R.drawable.ic_budgets, R.string.nav_budgets)
    object Reports : BottomNavItem(Route.REPORTS, R.drawable.ic_reports, R.string.nav_reports)
    object Settings : BottomNavItem(Route.SETTINGS, R.drawable.ic_settings, R.string.nav_settings)

    companion object {
        val items = listOf(Dashboard, Transactions, Budgets, Reports, Settings)
    }
}