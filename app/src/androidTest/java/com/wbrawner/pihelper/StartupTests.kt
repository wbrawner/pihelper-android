package com.wbrawner.pihelper

import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

@UninstallModules(PiHelperModule::class)
@HiltAndroidTest
@OptIn(ExperimentalAnimationApi::class)
class StartupTests {

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val apiService: FakeAPIService = FakeAPIService()

    @Test
    fun testManualConnectionWithPassword() {
        onAddScreen(composeTestRule) {
            apiService.server.enqueue(
                MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody(context.readAsset("json/version_success.json"))
            )
            clearHost()
            inputHost("${apiService.hostName}:${apiService.port}")
            clickConnect()
            val request = apiService.server.takeRequest(1, TimeUnit.SECONDS)
            assertTrue(request?.requestUrl?.queryParameterNames?.contains("version") ?: false)
        } onAuthScreen {
            verifyConnectionSuccessMessage()
            apiService.server.enqueue(
                MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody(context.readAsset("json/top_items_success.json"))
            )
            inputPassword("password")
            clickAuthenticateWithPassword()
            val request = apiService.server.takeRequest(1, TimeUnit.SECONDS)
            assertTrue(request?.requestUrl?.queryParameterNames?.contains("topItems") ?: false)
            assertEquals(
                "113459eb7bb31bddee85ade5230d6ad5d8b2fb52879e00a84ff6ae1067a210d3",
                request?.requestUrl?.queryParameter("auth")
            )
        }
    }

    @Test
    fun testManualConnectionWithAPIKey() {
        onAddScreen(composeTestRule) {
            val body = context.readAsset("json/version_success.json")
            apiService.server.enqueue(
                MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody(body)
            )
            clearHost()
            inputHost("${apiService.hostName}:${apiService.port}")
            clickConnect()
            val request = apiService.server.takeRequest(1, TimeUnit.SECONDS)
            assertTrue(request?.requestUrl?.queryParameterNames?.contains("version") ?: false)
        } onAuthScreen {
            verifyConnectionSuccessMessage()
            apiService.server.enqueue(
                MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody(context.readAsset("json/top_items_success.json"))
            )
            inputAPIKey("113459eb7bb31bddee85ade5230d6ad5d8b2fb52879e00a84ff6ae1067a210d3")
            clickAuthenticateWithAPIKey()
            val request = apiService.server.takeRequest(1, TimeUnit.SECONDS)
            assertTrue(request?.requestUrl?.queryParameterNames?.contains("topItems") ?: false)
            assertEquals(
                "113459eb7bb31bddee85ade5230d6ad5d8b2fb52879e00a84ff6ae1067a210d3",
                request?.requestUrl?.queryParameter("auth")
            )
        }
    }
}

fun Context.readAsset(path: String) = assets.open(path).bufferedReader().use { it.readText() }