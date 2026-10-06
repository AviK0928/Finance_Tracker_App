package com.example.finance_tracker.core.navigation

import android.app.Application
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The real NavigationActions on a test controller holding the app's main routes. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class NavigationActionsTest {

    private lateinit var navController: TestNavHostController
    private lateinit var actions: NavigationActions

    @Before
    fun setUp() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        navController.graph = navController.createGraph(startDestination = Route.DASHBOARD) {
            listOf(Route.DASHBOARD, Route.TRANSACTIONS, Route.BUDGETS, Route.REPORTS, Route.SETTINGS, Route.NOTIFICATIONS)
                .forEach { route -> composable(route) {} }
        }
        actions = NavigationActions(navController)
    }

    private fun backStack() = navController.currentBackStack.value.mapNotNull { it.destination.route }

    @Test
    fun switchingTabs_keepsOnlyDashboardUnderTheCurrentTab() {
        actions.navigateToTopLevel(Route.TRANSACTIONS)
        actions.navigateToTopLevel(Route.BUDGETS)
        actions.navigateToTopLevel(Route.SETTINGS)

        assertEquals(listOf(Route.DASHBOARD, Route.SETTINGS), backStack())
    }

    @Test
    fun backFromATab_returnsToDashboard_andBackOnDashboardLeavesNothingToPop() {
        actions.navigateToTopLevel(Route.TRANSACTIONS)
        actions.navigateToTopLevel(Route.REPORTS)

        navController.popBackStack()
        assertEquals(Route.DASHBOARD, navController.currentDestination?.route)

        // Nothing left: the activity finishes and the app goes to the background
        assertFalse(navController.popBackStack())
    }

    @Test
    fun tappingTheCurrentTabTwice_addsNothing() {
        actions.navigateToTopLevel(Route.TRANSACTIONS)
        actions.navigateToTopLevel(Route.TRANSACTIONS)

        assertEquals(listOf(Route.DASHBOARD, Route.TRANSACTIONS), backStack())
    }
}
