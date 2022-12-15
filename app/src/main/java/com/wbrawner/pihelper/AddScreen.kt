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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wbrawner.pihelper.shared.Action
import com.wbrawner.pihelper.shared.Store
import com.wbrawner.pihelper.ui.PihelperTheme
import java.net.Inet4Address

val emulatorBuildModels = listOf(
    "Android SDK built for x86",
    "sdk_gphone64_arm64"
)

@Composable
fun AddScreen(store: Store) {
    val context = LocalContext.current
    AddPiholeForm(
        scanNetwork = {
            // TODO: This needs to go in the Store
            if (BuildConfig.DEBUG && emulatorBuildModels.contains(Build.MODEL)) {
                // For emulators, just begin scanning the host machine directly
                store.dispatch(Action.Scan("10.0.2.2"))
                return@AddPiholeForm
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
        store.state.value.loading
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPiholeForm(
    scanNetwork: () -> Unit,
    connectToPihole: (String) -> Unit,
    loading: Boolean = false
) {
    val (host: String, setHost: (String) -> Unit) = remember { mutableStateOf("pi.hole") }
    Column(
        modifier = Modifier
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
        PrimaryButton(text = "Scan Network", onClick = scanNetwork)
        OrDivider()
        Text(
            text = "If you already know the IP address or host of your Pi-hole, you can also " +
                    "enter it below:",
            textAlign = TextAlign.Center
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = host,
            onValueChange = setHost,
            label = { Text("Pi-hole Host") }
        )
        PrimaryButton(text = "Connect to Pi-hole", onClick = { connectToPihole(host) })
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
@Preview
fun AddPiholeForm_Preview() {
    PihelperTheme(false) {
        AddPiholeForm(scanNetwork = {}, {})
    }
}

@Composable
@Preview
fun AddPiholeForm_DarkPreview() {
    PihelperTheme(true) {
        AddPiholeForm(scanNetwork = {}, {})
    }
}

@Composable
@Preview
fun OrDivider_Preview() {
    PihelperTheme(false) {
        OrDivider()
    }
}

@Composable
@Preview
fun OrDivider_DarkPreview() {
    PihelperTheme(true) {
        OrDivider()
    }
}