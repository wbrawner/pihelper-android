package com.wbrawner.pihelper.shared

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.wbrawner.pihelper.shared.ui.OrDivider
import com.wbrawner.pihelper.shared.ui.theme.PihelperTheme
import io.ktor.client.*
import io.ktor.client.engine.*
import java.math.BigInteger
import java.security.MessageDigest

actual fun String.hash(): String = BigInteger(
    1,
    MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
).toString(16).padStart(64, '0')

@Composable
@Preview
fun OrDivider_Preview() {
    PihelperTheme {
        OrDivider()
    }
}