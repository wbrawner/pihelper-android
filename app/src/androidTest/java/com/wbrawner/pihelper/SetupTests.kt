package com.wbrawner.pihelper

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.wbrawner.pihelper.shared.PiholeAPIService
import com.wbrawner.pihelper.shared.Store
import com.wbrawner.pihelper.util.FakeAPIService
import com.wbrawner.pihelper.util.onAddScreen
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@UninstallModules(PiHelperModule::class)
@HiltAndroidTest
@OptIn(ExperimentalAnimationApi::class)
class SetupTests {

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val apiService: FakeAPIService = FakeAPIService()

    @Test
    fun testSuccessfulConnectionWithPassword() {
        onAddScreen(composeTestRule) {
            apiService.testConnectionSuccess()
            clearHost()
            inputHost("${apiService.hostName}:${apiService.port}")
            clickConnect()
            apiService.server.takeRequest(1, TimeUnit.SECONDS)
        } onAuthScreen {
            apiService.authenticationSuccess(context)
            verifyConnectionSuccessMessage()
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
    fun testSuccessfulConnectionWithAPIKey() {
        onAddScreen(composeTestRule) {
            apiService.testConnectionSuccess()
            clearHost()
            inputHost("${apiService.hostName}:${apiService.port}")
            clickConnect()
            apiService.server.takeRequest(1, TimeUnit.SECONDS)
        } onAuthScreen {
            apiService.authenticationSuccess(context)
            verifyConnectionSuccessMessage()
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

    @Test
    fun testFailedConnection() {
        onAddScreen(composeTestRule) {
            clearHost()
            inputHost("localhost")
            clickConnect()
            verifyErrorMessageIsDisplayed("Failed to connect")
        }
    }

    @Test
    fun testInvalidHost() {
        onAddScreen(composeTestRule) {
            apiService.testConnectionFailure()
            clearHost()
            inputHost("${apiService.hostName}:${apiService.port}")
            clickConnect()
            verifyErrorMessageIsDisplayed("Host does not appear to be a valid Pi-hole")
        }
    }

    @Test
    fun testFailedAuthenticationWithAPIKey() {
        onAddScreen(composeTestRule) {
            apiService.testConnectionSuccess()
            clearHost()
            inputHost("${apiService.hostName}:${apiService.port}")
            clickConnect()
        } onAuthScreen {
            apiService.authenticationFailure(context)
            verifyConnectionSuccessMessage()
            inputAPIKey("113459eb7bb31bddee85ade5230d6ad5d8b2fb52879e00a84ff6ae1067a210d3")
            clickAuthenticateWithAPIKey()
            verifyErrorMessageIsDisplayed("Invalid credentials")
        }
    }

    @Test
    fun testFailedAuthenticationWithPassword() {
        onAddScreen(composeTestRule) {
            apiService.testConnectionSuccess()
            clearHost()
            inputHost("${apiService.hostName}:${apiService.port}")
            clickConnect()
        } onAuthScreen {
            verifyConnectionSuccessMessage()
            apiService.authenticationFailure(context)
            inputPassword("password")
            clickAuthenticateWithPassword()
            verifyErrorMessageIsDisplayed("Invalid credentials")
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class TestModule {

        @Binds
        @Singleton
        abstract fun bindsPiholeAPIService(apiService: FakeAPIService): PiholeAPIService

        companion object {
            @Provides
            @Singleton
            fun providesStore(
                apiService: PiholeAPIService
            ): Store = Store(apiService)
        }
    }
}