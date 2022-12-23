package com.wbrawner.pihelper

import android.content.Context
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry

class AuthScreenRobot(private val testRule: ComposeTestRule) {
    val context: Context = InstrumentationRegistry.getInstrumentation().context

    init {
        testRule.waitUntil {
            testRule
                .onAllNodesWithTag(AUTH_SCREEN_TAG)
                .fetchSemanticsNodes().size == 1
        }
    }

    fun verifyConnectionSuccessMessage() =
        testRule.onNode(hasTestTag(SUCCESS_TEXT_TAG)).assertExists()

    fun inputPassword(password: String) =
        testRule.onNode(hasTestTag(PASSWORD_INPUT_TAG)).performTextInput(password)

    fun clickAuthenticateWithPassword() = testRule.onNode(hasTestTag(PASSWORD_BUTTON_TAG))
        .performClick()

    fun inputAPIKey(key: String) =
        testRule.onNode(hasTestTag(API_KEY_INPUT_TAG)).performTextInput(key)

    fun clickAuthenticateWithAPIKey() = testRule.onNode(hasTestTag(API_KEY_BUTTON_TAG))
        .performClick()
}