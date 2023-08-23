package com.wbrawner.pihelper.util

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry
import com.wbrawner.pihelper.shared.ui.DISABLE_PERMANENT_BUTTON_TAG
import com.wbrawner.pihelper.shared.ui.ENABLE_BUTTON_TAG
import com.wbrawner.pihelper.shared.ui.MAIN_SCREEN_TAG
import com.wbrawner.pihelper.shared.ui.STATUS_TEXT_TAG

fun onMainScreen(testRule: ComposeTestRule, actions: MainScreenRobot.() -> Unit) =
    MainScreenRobot(testRule).apply { actions() }

class MainScreenRobot(private val testRule: ComposeTestRule) {
    val context: Context = InstrumentationRegistry.getInstrumentation().context

    init {
        testRule.waitUntil {
            testRule
                .onAllNodesWithTag(MAIN_SCREEN_TAG)
                .fetchSemanticsNodes().size == 1
        }
    }

    infix fun onSettingsScreen(actions: AuthScreenRobot.() -> Unit) = AuthScreenRobot(testRule)
        .run {
            actions()
        }

    fun verifyStatus(status: String) {
        testRule.waitUntil {
            testRule.onAllNodes(hasTestTag(STATUS_TEXT_TAG).and(hasText(status)))
                .fetchSemanticsNodes().size == 1
        }
    }

    fun clickEnableButton() = testRule.onNode(hasTestTag(ENABLE_BUTTON_TAG)).performClick()

    fun clickDisablePermanentlyButton() = testRule.onNode(hasTestTag(DISABLE_PERMANENT_BUTTON_TAG))
        .performClick()

    fun verifyErrorMessageIsDisplayed(message: String) {
        testRule.waitUntil(2_000) {
            testRule
                .onAllNodesWithText(message, substring = true)
                .fetchSemanticsNodes().size == 1
        }
    }
}