package com.example.finance_tracker.core.ui.components

import android.app.Application
import android.app.DatePickerDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class DatePickerFieldTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tappingTheField_opensThePicker_atTheShownDate() {
        composeRule.setContent {
            DatePickerField(selectedDate = LocalDate.of(2026, 10, 6), onDateSelected = {}, label = "Start Date")
        }

        composeRule.onNodeWithText("2026-10-06").performClick()
        composeRule.waitForIdle()

        val dialog = ShadowDialog.getLatestDialog() as DatePickerDialog
        assertTrue(dialog.isShowing)
        assertEquals(2026, dialog.datePicker.year)
        assertEquals(9, dialog.datePicker.month) // 0-based: October
        assertEquals(6, dialog.datePicker.dayOfMonth)
    }
}
