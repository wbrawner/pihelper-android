import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberTrayState
import com.wbrawner.pihelper.shared.*
import com.wbrawner.pihelper.shared.State
import com.wbrawner.pihelper.shared.ui.AddScreen
import com.wbrawner.pihelper.shared.ui.AuthScreen
import com.wbrawner.pihelper.shared.ui.InfoScreen
import com.wbrawner.pihelper.shared.ui.MainScreen
import com.wbrawner.pihelper.shared.ui.theme.PihelperTheme
import java.net.InetAddress

val store = Store(PiholeAPIService.create())

@OptIn(ExperimentalAnimationApi::class)
@Composable
@Preview
fun App(state: State) {
    val error by store.effects.collectAsState(Effect.Empty)

    PihelperTheme {
        when (state.route) {
            Route.CONNECT -> AddScreen(
                scanNetwork = {
                    store.dispatch(Action.Scan(InetAddress.getLocalHost().hostAddress))
                },
                connectToPihole = { store.dispatch(Action.Connect(it)) },
                loading = state.loading,
                error = error as? Effect.Error
            )

            Route.AUTH -> AuthScreen(store)
            Route.HOME -> MainScreen(store)
            Route.ABOUT -> InfoScreen(store)
            else -> {
                Text("Not yet implemented")
            }
        }
    }
}

fun main() = application {
    LaunchedEffect(Unit) {
        System.setProperty("apple.awt.enableTemplateImages", "true")
    }
    val state by store.state.collectAsState()
    val trayState = rememberTrayState()
    var isOpen by remember { mutableStateOf(state.apiKey.isNullOrBlank()) }
    val statusText = when (val status = state.status) {
        is Status.Enabled -> "Enabled"
        is Status.Disabled -> status.timeRemaining?.let { "Disabled (${it})" } ?: "Disabled"
        else -> "Not connected"
    }
    Tray(
        state = trayState,
        tooltip = statusText,
        icon = painterResource("IconTemplate.png"),
        menu = {
            Item(
                text = statusText,
                enabled = false,
                onClick = {}
            )
            if (state.status is Status.Disabled) {
                Item(
                    "Enable",
                    onClick = {
                        store.dispatch(Action.Enable)
                    }
                )
            } else if (state.status is Status.Enabled) {
                Item(
                    "Disable for 10 seconds",
                    onClick = {
                        store.dispatch(Action.Disable(duration = 10))
                    }
                )
                Item(
                    "Disable for 30 seconds",
                    onClick = {
                        store.dispatch(Action.Disable(duration = 30))
                    }
                )
                Item(
                    "Disable for 1 minute",
                    onClick = {
                        store.dispatch(Action.Disable(duration = 30))
                    }
                )
                Item(
                    "Disable for 5 minutes",
                    onClick = {
                        store.dispatch(Action.Disable(duration = 30))
                    }
                )
                Item(
                    "Disable permanently",
                    onClick = {
                        store.dispatch(Action.Disable())
                    }
                )
            }
            Item(
                text = if (isOpen) "Hide window" else "Show window",
                onClick = {
                    isOpen = !isOpen
                }
            )
            Item(
                "Exit",
                onClick = {
                    isOpen = false
                }
            )
        }
    )

    if (isOpen) {
        Window(
            title = "Pi-helper",
            onCloseRequest = {
                isOpen = false
            }) {
            App(state)
        }
    }
}
