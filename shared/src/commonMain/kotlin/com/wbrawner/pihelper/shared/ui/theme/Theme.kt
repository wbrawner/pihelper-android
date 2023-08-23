package com.wbrawner.pihelper.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
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
