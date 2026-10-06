package com.example.finance_tracker.core.ui.components

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Compose on the JVM via Robolectric. SDK 34 and a plain Application for the same reasons as SyncRepoImplTest. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class TextFieldWithLabelsTest {

    @get:Rule
    val composeRule = createComposeRule()

    // What the field displays. (hasText would also match InputText, the raw value, even while masked.)
    private fun displays(text: String) =
        SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(text))

    // Set by the password keyboard type: tells the keyboard and autofill this is a password field.
    private val isPasswordField = SemanticsMatcher.keyIsDefined(SemanticsProperties.Password)

    private fun field(isPassword: Boolean) {
        composeRule.setContent {
            var value by remember { mutableStateOf("") }
            TextFieldWithLabels(value = value, onValueChange = { value = it }, label = "Password", isPassword = isPassword)
        }
        composeRule.onNode(hasSetTextAction()).performTextInput("Secret1!")
    }

    @Test
    fun passwordField_isMaskedByDefault_andDeclaredAsPassword() {
        field(isPassword = true)

        composeRule.onNode(hasSetTextAction())
            .assert(displays("Secret1!").not())
            .assert(isPasswordField)
    }

    @Test
    fun eyeIcon_revealsThePassword_andASecondTapMasksItAgain() {
        field(isPassword = true)

        composeRule.onNodeWithContentDescription("Show password").performClick()
        composeRule.onNode(hasSetTextAction()).assert(displays("Secret1!"))

        composeRule.onNodeWithContentDescription("Hide password").performClick()
        composeRule.onNode(hasSetTextAction()).assert(displays("Secret1!").not())
    }

    @Test
    fun plainField_showsTheText_andHasNoEyeIcon() {
        field(isPassword = false)

        composeRule.onNode(hasSetTextAction())
            .assert(displays("Secret1!"))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Password))
        composeRule.onNodeWithContentDescription("Show password").assertDoesNotExist()
    }
}
