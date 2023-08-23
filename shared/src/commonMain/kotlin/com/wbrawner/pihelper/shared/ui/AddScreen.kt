package com.wbrawner.pihelper.shared.ui

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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wbrawner.pihelper.shared.Effect
import com.wbrawner.pihelper.shared.ui.component.LoadingSpinner
import com.wbrawner.pihelper.shared.ui.component.PrimaryButton

const val ADD_SCREEN_TAG = "addScreen"
const val CONNECT_BUTTON_TAG = "connectButton"
const val HOST_TAG = "hostInput"
const val SCAN_BUTTON_TAG = "scanButton"

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
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
            modifier = Modifier.semantics { contentDescription = "Scan Network button" },
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
                .semantics { contentDescription = "Pi-hole host input" }
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
