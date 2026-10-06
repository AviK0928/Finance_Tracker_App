package com.example.finance_tracker.core.ui.components

import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class RefreshableBoxTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun pullingDownOnTheList_reloadsOnce() {
        var refreshes = 0
        composeRule.setContent {
            RefreshableBox(onRefresh = { refreshes++ }, modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.fillMaxSize().testTag("list")) {
                    items(3) { Text("Row $it") }
                }
            }
        }

        composeRule.onNodeWithTag("list").performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        assertEquals(1, refreshes)
    }
}
