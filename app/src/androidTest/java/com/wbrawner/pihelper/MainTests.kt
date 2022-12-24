package com.wbrawner.pihelper

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.wbrawner.pihelper.shared.PiholeAPIService
import com.wbrawner.pihelper.shared.State
import com.wbrawner.pihelper.shared.Store
import com.wbrawner.pihelper.util.FakeAPIService
import com.wbrawner.pihelper.util.onMainScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@UninstallModules(PiHelperModule::class)
@HiltAndroidTest
@OptIn(ExperimentalAnimationApi::class)
class MainTests {

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 0)
    val hiltTestRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val apiService: FakeAPIService = FakeAPIService()

    @Test
    fun testDisable() {
        onMainScreen(composeTestRule) {
            apiService.statusEnabled(context)
            apiService.disableSuccess(context)
            apiService.statusDisabled(context)
            apiService.disabledPermanently()
            verifyStatus("Enabled")
            val request = apiService.server.takeRequest(1, TimeUnit.SECONDS)
            assertTrue(request?.requestUrl?.queryParameterNames?.contains("auth") == true)
            clickDisablePermanentlyButton()
            verifyStatus("Disabled")
        }
    }

    @Test
    fun testEnable() {
        onMainScreen(composeTestRule) {
            apiService.statusDisabled(context)
            apiService.disabledPermanently()
            apiService.enableSuccess(context)
            apiService.statusEnabled(context)
            verifyStatus("Disabled")
            val request = apiService.server.takeRequest(1, TimeUnit.SECONDS)
            assertTrue(request?.requestUrl?.queryParameterNames?.contains("auth") == true)
            clickEnableButton()
            verifyStatus("Enabled")
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    inner class TestModule {

        @Provides
        @Singleton
        fun providesPiholeAPIService(): PiholeAPIService = apiService

        @Provides
        @Singleton
        fun providesInitialState(apiService: FakeAPIService): State = State(
            apiKey = "key",
            host = "${apiService.hostName}:${apiService.port}"
        )

        @Provides
        @Singleton
        fun providesStore(
            apiService: PiholeAPIService,
            initialState: State
        ): Store = Store(apiService, initialState = initialState)
    }
}