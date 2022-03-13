package com.wbrawner.pihelper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wbrawner.pihelper.shared.Store
import com.wbrawner.pihelper.ui.PihelperTheme

@Composable
fun ScanScreen(store: Store) {
    ScanningStatus(store.state.value.scanning?.let { "Scanning $it..." })
}

@Composable
fun ScanningStatus(
    loadingMessage: String? = null
) {
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
