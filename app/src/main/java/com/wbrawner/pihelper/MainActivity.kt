package com.wbrawner.pihelper

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wbrawner.pihelper.ui.PihelperTheme
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalAnimationApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkTheme = isSystemInDarkTheme()
            LaunchedEffect(key1 = isDarkTheme) {
                if (isDarkTheme) return@LaunchedEffect
                window.navigationBarColor = ContextCompat.getColor(this@MainActivity, R.color.colorSurface)
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
            val navController = rememberNavController()
            val addPiHoleViewModel: AddPiHelperViewModel = viewModel()
            val startDestination = when {
                addPiHoleViewModel.baseUrl == null -> {
                    Screens.ADD
                }
                addPiHoleViewModel.apiKey == null -> {
                    Screens.AUTH
                }
                else -> {
                    Screens.MAIN
                }
            }
            PihelperTheme {
                NavHost(navController, startDestination = startDestination.route) {
                    composable(Screens.ADD.route) {
                        AddScreen(navController, addPiHoleViewModel)
                    }
                    composable(Screens.SCAN.route) {
                        ScanScreen(navController, addPiHoleViewModel)
                    }
                    composable(Screens.AUTH.route) {
                        AuthScreen(
                            navController = navController,
                            addPiHelperViewModel = addPiHoleViewModel
                        )
                    }
                    composable(Screens.MAIN.route) {
                        MainScreen(navController = navController)
                    }
                    composable(Screens.INFO.route) {
                        InfoScreen(
                            navController = navController,
                            addPiHelperViewModel = addPiHoleViewModel
                        )
                    }
                }
            }
        }
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