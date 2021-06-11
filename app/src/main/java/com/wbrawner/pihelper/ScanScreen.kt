package com.wbrawner.pihelper

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wbrawner.pihelper.ui.PihelperTheme
import kotlinx.coroutines.launch
import java.net.Inet4Address

@Composable
fun ScanScreen(navController: NavController, addPiHelperViewModel: AddPiHelperViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val loadingMessage by addPiHelperViewModel.loadingMessage.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(key1 = addPiHelperViewModel) {
        val onSuccess: () -> Unit = {
            navController.navigate(Screens.AUTH.route)
        }
        val onFailure: () -> Unit = {
            navController.popBackStack()
        }
        if (BuildConfig.DEBUG && Build.MODEL == "Android SDK built for x86") {
            // For emulators, just begin scanning the host machine directly
            coroutineScope.launch {
                addPiHelperViewModel.beginScanning("10.0.2.2", onSuccess, onFailure)
            }
            return@LaunchedEffect
        }
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.let { connectivityManager ->
            connectivityManager.allNetworks
                .filter {
                    connectivityManager.getNetworkCapabilities(it)
                        ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        ?: false
                }
                .forEach { network ->
                    connectivityManager.getLinkProperties(network)
                        ?.linkAddresses
                        ?.filter { !it.address.isLoopbackAddress && it.address is Inet4Address }
                        ?.forEach { address ->
                            Log.d(
                                "Pi-helper",
                                "Found link address: ${address.address.hostName}"
                            )
                            addPiHelperViewModel.beginScanning(
                                address.address.hostAddress,
                                onSuccess,
                                onFailure
                            )
                        }
                }
        }
    }
    ScanningStatus(loadingMessage)
}

@Composable
fun ScanningStatus(
    loadingMessage: String? = null
) {
    val (host: String, setHost: (String) -> Unit) = remember { mutableStateOf("pi.hole") }
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        LoadingSpinner(loadingMessage != null)
        loadingMessage?.let {
            Text(
                text = loadingMessage,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
@Preview
fun ScanningStatus_Preview() {
    PihelperTheme(false) {
        ScanningStatus(loadingMessage = "Scanning 127.0.0.1")
    }
}

@Composable
@Preview
fun ScanningStatus_DarkPreview() {
    PihelperTheme(true) {
        ScanningStatus(loadingMessage = "Scanning 127.0.0.1")
    }
}
