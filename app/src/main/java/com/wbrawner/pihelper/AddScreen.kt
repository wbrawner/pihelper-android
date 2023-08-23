package com.wbrawner.pihelper

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.wbrawner.pihelper.shared.Action
import com.wbrawner.pihelper.shared.Effect
import com.wbrawner.pihelper.shared.Store
import com.wbrawner.pihelper.shared.ui.AddScreen
import com.wbrawner.pihelper.shared.ui.OrDivider
import com.wbrawner.pihelper.shared.ui.theme.PihelperTheme
import java.net.Inet4Address

val emulatorBuildModels = listOf(
    "Android SDK built for x86",
    "sdk_gphone64_arm64"
)

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