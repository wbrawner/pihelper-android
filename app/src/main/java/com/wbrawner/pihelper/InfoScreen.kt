package com.wbrawner.pihelper

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.smallTopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wbrawner.pihelper.shared.*
import com.wbrawner.pihelper.ui.PihelperTheme

@Composable
fun InfoScreen(store: Store) {
    InfoScreen(
        onBackClicked = { store.dispatch(Action.Back) },
        onForgetPiholeClicked = { store.dispatch(Action.Forget) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(onBackClicked: () -> Unit, onForgetPiholeClicked: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = { Text("About Pi-helper") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            LoadingSpinner()
            val message = buildAnnotatedString {
                val text =
                    "Pi-helper was made with ❤ by William Brawner. You can find the source " +
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
                        color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ),
                    start = github,
                    end = github + 11,
                )
            }
            val uriHandler = LocalUriHandler.current
            ClickableText(
                text = message,
                style = TextStyle.Default.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                ),
            ) {
                message.getStringAnnotations(it, it).firstOrNull()?.let { annotation ->
                    uriHandler.openUri(annotation.item)
                    // TODO: Move this to the store?
                    PlausibleAnalyticsHelper.event(
                        AnalyticsEvent.LinkClicked(annotation.item),
                        Route.ABOUT
                    )
                }
            }
            TextButton(onClick = onForgetPiholeClicked) {
                Text(text = "Forget Pi-hole")
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
fun InfoScreen_Preview() {
    PihelperTheme {
        InfoScreen({}, {})
    }
}
