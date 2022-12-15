package com.wbrawner.pihelper.ui

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

private val DarkColorPalette = darkColorScheme(
    background = Color.Black,
    surface = Color.Black,
    primary = Red500,
//    primaryVariant = Red900,
    onPrimary = Color.White,
    secondary = Green500,
//    secondaryVariant = Green900,
    onSecondary = Color.White
)

private val LightColorPalette = lightColorScheme(
    primary = Red500,
//    primaryVariant = Red900,
    onPrimary = Color.White,
    secondary = Green500,
//    secondaryVariant = Green900,
    onSecondary = Color.White
)

@Composable
fun PihelperTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dynamic = false
    val colors = if (dynamic) {
        if (darkTheme) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    } else {
        if (darkTheme) {
            DarkColorPalette
        } else {
            LightColorPalette
        }
    }

    MaterialTheme(
        colorScheme = colors,
//        typography = Typography,
//        shapes = Shapes,
        content = {
            Surface(color = MaterialTheme.colorScheme.background, content = content)
        }
    )
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES)
annotation class DayNightPreview