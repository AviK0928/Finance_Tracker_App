package com.example.finance_tracker.core.navigation

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** AuthContainer in a 300 x 400 dp area (fits Robolectric's default screen). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class AuthContainerTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun showForm(formHeight: Int) {
        composeRule.setContent {
            Box(modifier = Modifier.size(width = 300.dp, height = 400.dp)) {
                AuthContainer {
                    Box(modifier = Modifier.fillMaxWidth().height(formHeight.dp).testTag("form"))
                }
            }
        }
    }

    @Test
    fun shortForm_isCenteredVertically() {
        showForm(formHeight = 100)

        // Centered: 16 + (400 - 32 - 100) / 2 = 150 dp from the top. Top-aligned it was 16 dp.
        val top = composeRule.onNodeWithTag("form").getUnclippedBoundsInRoot().top
        assertTrue("form top was $top", top > 100.dp)
    }

    @Test
    fun tallForm_startsAtTheTop_soNothingIsPushedOutOfReach() {
        showForm(formHeight = 1000)

        val top = composeRule.onNodeWithTag("form").getUnclippedBoundsInRoot().top
        assertTrue("form top was $top", top < 50.dp)
    }
}
