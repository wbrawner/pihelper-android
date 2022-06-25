package com.wbrawner.pihelper

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.animation.AnticipateInterpolator
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wbrawner.pihelper.shared.Action
import com.wbrawner.pihelper.shared.Effect
import com.wbrawner.pihelper.shared.Store
import com.wbrawner.pihelper.ui.PihelperTheme
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
            val state by store.state.collectAsState()
            val navController = rememberNavController()
            val startDestination = when {
                state.host == null -> {
                    Screens.ADD
                }
                state.apiKey == null -> {
                    Screens.AUTH
                }
                else -> {
                    Screens.MAIN
                }
            }
            LaunchedEffect(state) {
                if (!state.scanning.isNullOrBlank()) {
                    navController.navigateIfNotAlreadyThere(Screens.SCAN.route)
                } else if (state.host == null) {
                    navController.navigateIfNotAlreadyThere(Screens.ADD.route)
                } else if (state.apiKey == null) {
                    navController.navigateIfNotAlreadyThere(Screens.AUTH.route)
                } else if (state.showAbout) {
                    navController.navigateIfNotAlreadyThere(Screens.INFO.route)
                } else if (state.status != null) {
                    navController.navigateIfNotAlreadyThere(Screens.MAIN.route)
                }
            }
            val effect by store.effects.collectAsState(initial = Effect.Empty)
            val context = LocalContext.current
            LaunchedEffect(effect) {
                when (effect) {
                    is Effect.Error -> Toast.makeText(
                        context,
                        (effect as Effect.Error).message,
                        Toast.LENGTH_SHORT
                    ).show()
                    is Effect.Exit -> finish()
                }
            }
            PihelperTheme {
                NavHost(navController, startDestination = startDestination.route) {
                    composable(Screens.ADD.route) {
                        AddScreen(store)
                    }
                    composable(Screens.SCAN.route) {
                        ScanScreen(store)
                    }
                    composable(Screens.AUTH.route) {
                        AuthScreen(store)
                    }
                    composable(Screens.MAIN.route) {
                        MainScreen(store)
                    }
                    composable(Screens.INFO.route) {
                        InfoScreen(store)
                    }
                }
            }
        }
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

    override fun onBackPressed() {
        store.dispatch(Action.Back)
    }
}

enum class Screens(val route: String) {
    ADD("add"),
    SCAN("scan"),
    AUTH("auth"),
    MAIN("main"),
    INFO("info"),
}

@Composable
fun LoadingSpinner(animate: Boolean = false) {
    val animation = rememberInfiniteTransition()
    val rotation by animation.animateValue(
        initialValue = 0f,
        targetValue = 360f,
        typeConverter = Float.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    Image(
        modifier = Modifier.rotate(if (animate) rotation else 0f),
        painter = painterResource(id = R.drawable.ic_app_logo),
        contentDescription = "Loading",
        colorFilter = ColorFilter.tint(MaterialTheme.colors.onBackground)
    )
}

@Composable
@Preview
fun LoadingSpinner_Preview() {
    LoadingSpinner()
}

fun NavController.navigateIfNotAlreadyThere(route: String) {
    if (currentDestination?.route != route) {
        navigate(route)
    }
}