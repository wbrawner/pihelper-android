package com.wbrawner.pihelper

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.animation.AnticipateInterpolator
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wbrawner.pihelper.shared.Action
import com.wbrawner.pihelper.shared.Effect
import com.wbrawner.pihelper.shared.Route
import com.wbrawner.pihelper.shared.Store
import com.wbrawner.pihelper.shared.ui.AuthScreen
import com.wbrawner.pihelper.shared.ui.InfoScreen
import com.wbrawner.pihelper.shared.ui.MainScreen
import com.wbrawner.pihelper.shared.ui.component.LoadingSpinner
import com.wbrawner.pihelper.shared.ui.theme.PihelperTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ExperimentalAnimationApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var store: Store

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkTheme = isSystemInDarkTheme()
            LaunchedEffect(key1 = isDarkTheme) {
                if (isDarkTheme) return@LaunchedEffect
                window.navigationBarColor =
                    ContextCompat.getColor(this@MainActivity, R.color.colorSurface)
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    window.insetsController?.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    )
                } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            }
            val launchIntent = remember { intent }
            LaunchedEffect(launchIntent) {
                ShortcutActions.fromIntentAction(launchIntent.action)?.let { action ->
                    if (action == ShortcutActions.DISABLE) {
                        val duration = launchIntent.getIntExtra(DURATION, 0)
                        store.dispatch(Action.Disable(duration.toLong()))
                    } else {
                        store.dispatch(Action.Enable)
                    }
                }
            }
            val state by store.state.collectAsState()
            val navController = rememberNavController()
            LaunchedEffect(state.route) {
                navController.navigate(state.route.name)
            }
            val effect by store.effects.collectAsState(initial = Effect.Empty)
            LaunchedEffect(effect) {
                when (effect) {
                    is Effect.Exit -> finish()
                    else -> {
                        // no-op
                    }
                }
            }
            PihelperTheme {
                NavHost(navController, startDestination = state.initialRoute.name) {
                    composable(Route.CONNECT.name) {
                        AddScreen(store)
                    }
                    composable(Route.SCAN.name) {
                        ScanScreen(store)
                    }
                    composable(Route.AUTH.name) {
                        AuthScreen(store)
                    }
                    composable(Route.HOME.name) {
                        MainScreen(store)
                    }
                    composable(Route.ABOUT.name) {
                        InfoScreen(store)
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                listOf(View.SCALE_X, View.SCALE_Y).forEach { axis ->
                    ObjectAnimator.ofFloat(
                        splashScreenView,
                        axis,
                        1f,
                        0.45f
                    ).apply {
                        interpolator = AnticipateInterpolator()
                        duration = 200L
                        doOnEnd {
                            splashScreenView.remove()
                        }
                        start()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        store.dispatch(Action.Back)
    }
}

@Composable
@Preview
fun LoadingSpinner_Preview() {
    LoadingSpinner()
}

enum class ShortcutActions(val fullName: String) {
    ENABLE("com.wbrawner.pihelper.ShortcutActions.ENABLE"),
    DISABLE("com.wbrawner.pihelper.ShortcutActions.DISABLE");

    companion object {
        fun fromIntentAction(action: String?): ShortcutActions? = when (action) {
            ENABLE.fullName -> ENABLE
            DISABLE.fullName -> DISABLE
            else -> null
        }
    }
}

const val DURATION: String = "com.wbrawner.pihelper.MainActivityKt.DURATION"

@Composable
@DayNightPreview
fun InfoScreen_Preview() {
    PihelperTheme {
        InfoScreen({}, {})
    }
}
