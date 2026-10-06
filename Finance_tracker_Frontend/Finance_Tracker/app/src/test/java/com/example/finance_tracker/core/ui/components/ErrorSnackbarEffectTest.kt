package com.example.finance_tracker.core.ui.components

import android.app.Application
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ErrorSnackbarEffectTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun error_isShown_thenHiddenAndClearedOnItsOwn() {
        var message by mutableStateOf<String?>("Could not load budgets")
        var cleared = 0
        // Drive time by hand so the snackbar's few seconds can be checked on both sides
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            val hostState = remember { SnackbarHostState() }
            ErrorSnackbarEffect(message, hostState) {
                cleared++
                message = null
            }
            SnackbarHost(hostState)
        }

        composeRule.mainClock.advanceTimeBy(500)
        composeRule.onNodeWithText("Could not load budgets").assertExists()
        assertEquals(0, cleared)

        composeRule.mainClock.advanceTimeBy(10_000)
        composeRule.onNodeWithText("Could not load budgets").assertDoesNotExist()
        assertEquals(1, cleared)
    }
}
