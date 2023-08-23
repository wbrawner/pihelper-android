package com.wbrawner.pihelper.util

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry
import com.wbrawner.pihelper.CONNECT_BUTTON_TAG

fun onAddScreen(testRule: ComposeTestRule, actions: AddScreenRobot.() -> Unit) =
    AddScreenRobot(testRule).apply { actions() }

class AddScreenRobot(private val testRule: ComposeTestRule) {
    val context: Context = InstrumentationRegistry.getInstrumentation().context

    infix fun onAuthScreen(actions: AuthScreenRobot.() -> Unit) = AuthScreenRobot(testRule).run {
        actions()
    }

    fun clearHost() =
        testRule.onNodeWithContentDescription("Pi-hole host input").performTextClearance()

    fun inputHost(host: String) =
        testRule.onNodeWithContentDescription("Pi-hole host input").performTextInput(host)

    fun clickConnect() = testRule.onNode(hasTestTag(CONNECT_BUTTON_TAG)).performClick()

    fun verifyErrorMessageIsDisplayed(message: String) {
        testRule.waitUntil(2_000) {
            testRule
                .onAllNodesWithContentDescription(message, substring = true)
                .fetchSemanticsNodes().size == 1
        }
    }
}