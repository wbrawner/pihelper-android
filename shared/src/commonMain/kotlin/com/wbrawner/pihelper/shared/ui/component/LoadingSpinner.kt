package com.wbrawner.pihelper.shared.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
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
        painter = painterResource(DrawableResource("img/ic_app_logo.xml")),
        contentDescription = "Loading",
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
    )
}
