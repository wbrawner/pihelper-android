package com.wbrawner.pihelper

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry

fun onAddScreen(testRule: ComposeTestRule, actions: AddScreenRobot.() -> Unit) =
    AddScreenRobot(testRule).apply { actions() }

class AddScreenRobot(private val testRule: ComposeTestRule) {
    val context: Context = InstrumentationRegistry.getInstrumentation().context

    init {
        testRule.waitUntil {
            testRule
                .onAllNodesWithTag(ADD_SCREEN_TAG)
                .fetchSemanticsNodes().size == 1
        }
    }

    infix fun onAuthScreen(actions: AuthScreenRobot.() -> Unit) = AuthScreenRobot(testRule).run {
        actions()
    }

    fun clearHost() =
        testRule.onNode(hasTestTag(HOST_TAG)).performTextClearance()

    fun inputHost(host: String) =
        testRule.onNode(hasTestTag(HOST_TAG)).performTextInput(host)

    fun clickConnect() = testRule.onNode(hasTestTag(CONNECT_BUTTON_TAG)).performClick()

}