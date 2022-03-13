package com.wbrawner.pihelper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.wbrawner.pihelper.shared.Action
import com.wbrawner.pihelper.shared.Store

@Composable
fun InfoScreen(store: Store) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        LoadingSpinner()
        val message = buildAnnotatedString {
            val text = "Pi-helper was made with ❤ by William Brawner. You can find the source " +
                    "code or report issues on the GitHub page for the project."
            val name = text.indexOf("William")
            val github = text.indexOf("GitHub")
            append(text)
            addStringAnnotation(
                "me",
                annotation = "https://wbrawner.com",
                start = name,
                end = name + 15
            )
            addStyle(
                style = SpanStyle(
                    color = MaterialTheme.colors.primary,
                    textDecoration = TextDecoration.Underline
                ),
                start = name,
                end = name + 15
            )
            addStringAnnotation(
                "github",
                annotation = "https://github.com/wbrawner/pihelper-android",
                start = github,
                end = github + 11,
            )
            addStyle(
                style = SpanStyle(
                    color = MaterialTheme.colors.primary,
                    textDecoration = TextDecoration.Underline
                ),
                start = github,
                end = github + 11,
            )
        }
        val uriHandler = LocalUriHandler.current
        ClickableText(
            text = message,
            style = TextStyle.Default.copy(textAlign = TextAlign.Center)
        ) {
            message.getStringAnnotations(it, it).firstOrNull()?.let { annotation ->
                uriHandler.openUri(annotation.item)
            }
        }
        TextButton(onClick = { store.dispatch(Action.Forget) }) {
            Text(text = "Forget Pi-hole")
        }
    }
}
