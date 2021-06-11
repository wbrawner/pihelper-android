package com.wbrawner.pihelper

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(navController: NavController, addPiHelperViewModel: AddPiHelperViewModel) {
    val (password: String, setPassword: (String) -> Unit) = remember { mutableStateOf("") }
    val (apiKey: String, setApiKey: (String) -> Unit) = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_app_logo),
            contentDescription = null
        )
        Text(
            text = "Pi-helper has successfully connected to your Pi-Hole!",
            textAlign = TextAlign.Center
        )
        Text(
            text = "You'll need to authenticate in order to enable and disable the Pi-hole.",
            textAlign = TextAlign.Center
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            onValueChange = setPassword,
            label = { Text("Pi-hole Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        PrimaryButton(text = "Authenticate with Password") {
            coroutineScope.launch {
                if (addPiHelperViewModel.authenticateWithPassword(password)) {
                    navController.navigate(Screens.MAIN.route)
                }
            }
        }
        OrDivider()
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = apiKey,
            onValueChange = setApiKey,
            label = { Text("Pi-hole API Key") },
            visualTransformation = PasswordVisualTransformation(),
        )
        PrimaryButton(text = "Authenticate with API Key") {
            coroutineScope.launch {
                if (addPiHelperViewModel.authenticateWithApiKey(password)) {
                    navController.navigate(Screens.MAIN.route)
                }
            }
        }
    }
}
