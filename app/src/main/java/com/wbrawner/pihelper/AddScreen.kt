package com.wbrawner.pihelper

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wbrawner.pihelper.shared.Action
import com.wbrawner.pihelper.shared.Effect
import com.wbrawner.pihelper.shared.Store
import com.wbrawner.pihelper.ui.DayNightPreview
import com.wbrawner.pihelper.ui.PihelperTheme
import java.net.Inet4Address

val emulatorBuildModels = listOf(
    "Android SDK built for x86",
    "sdk_gphone64_arm64"
)

const val ADD_SCREEN_TAG = "addScreen"
const val CONNECT_BUTTON_TAG = "connectButton"
const val HOST_TAG = "hostInput"
const val SCAN_BUTTON_TAG = "scanButton"

@Composable
fun AddScreen(store: Store) {
    val effect by store.effects.collectAsState(initial = Effect.Empty)
    val context = LocalContext.current
    AddScreen(
        scanNetwork = scan@{
            // TODO: This needs to go in the Store
            if (BuildConfig.DEBUG && emulatorBuildModels.contains(Build.MODEL)) {
                // For emulators, just begin scanning the host machine directly
                store.dispatch(Action.Scan("10.0.2.2"))
                return@scan
            }
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)
                ?.let { connectivityManager ->
                    connectivityManager.allNetworks
                        .filter {
                            connectivityManager.getNetworkCapabilities(it)
                                ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                                ?: false
                        }
                        .mapNotNull { network ->
                            connectivityManager.getLinkProperties(network)
                                ?.linkAddresses
                                ?.filter {
                                    !it.address.isLoopbackAddress
                                            && !it.address.isLinkLocalAddress
                                            && it.address is Inet4Address
                                }
                                ?.mapNotNull { it.address.hostAddress }
                                ?.forEach {
                                    store.dispatch(Action.Scan(it))
                                }
                        }
                }
                ?: Toast.makeText(context, "Failed to scan network", Toast.LENGTH_SHORT).show()
        },
        connectToPihole = {
            store.dispatch(Action.Connect(it))
        },
        store.state.value.loading,
        error = effect as? Effect.Error
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AddScreen(
    scanNetwork: () -> Unit,
    connectToPihole: (String) -> Unit,
    loading: Boolean = false,
    error: Effect.Error? = null
) {
    val (host: String, setHost: (String) -> Unit) = remember { mutableStateOf("pi.hole") }
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier
            .testTag(ADD_SCREEN_TAG)
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        LoadingSpinner(loading)
        Text(
            text = "If you're not sure what the IP address for your Pi-hole is, Pi-helper can " +
                    "attempt to find it for you by scanning your network.",
            textAlign = TextAlign.Center
        )
        PrimaryButton(
            modifier = Modifier.testTag(SCAN_BUTTON_TAG),
            text = "Scan Network",
            onClick = scanNetwork
        )
        OrDivider()
        Text(
            text = "If you already know the IP address or host of your Pi-hole, you can also " +
                    "enter it below:",
            textAlign = TextAlign.Center
        )
        OutlinedTextField(
            modifier = Modifier
                .testTag(HOST_TAG)
                .fillMaxWidth(),
            value = host,
            onValueChange = setHost,
            label = { Text("Pi-hole Host") }
        )
        PrimaryButton(
            modifier = Modifier.testTag(CONNECT_BUTTON_TAG),
            text = "Connect to Pi-hole",
            onClick = {
                keyboardController?.hide()
                connectToPihole(host)
            }
        )
        error?.let {
            Text(
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                text = "Connection failed: ${it.message}"
            )
        }
    }
}

@Composable
fun OrDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .height(2.dp)
                .weight(1f)
                .padding(end = 8.dp)
                .clip(RectangleShape)
                .background(MaterialTheme.colorScheme.onSurface),
        )
        Text("OR")
        Box(
            modifier = Modifier
                .height(2.dp)
                .weight(1f)
                .padding(start = 8.dp)
                .clip(RectangleShape)
                .background(MaterialTheme.colorScheme.onSurface),
        )
    }
}

@Composable
@DayNightPreview
fun AddScreen_Preview() {
    PihelperTheme {
        AddScreen({}, {}, error = Effect.Error("Something bad happened"))
    }
}

@Composable
@DayNightPreview
fun OrDivider_Preview() {
    PihelperTheme {
        OrDivider()
    }
}