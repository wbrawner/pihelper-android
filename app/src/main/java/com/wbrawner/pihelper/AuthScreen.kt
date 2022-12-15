package com.wbrawner.pihelper

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wbrawner.pihelper.shared.Action
import com.wbrawner.pihelper.shared.AuthenticationString
import com.wbrawner.pihelper.shared.Store
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(store: Store) {
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
            modifier = Modifier
                .fillMaxWidth()
                .autofill(listOf(AutofillType.Password), onFill = setPassword),
            value = password,
            onValueChange = setPassword,
            label = { Text("Pi-hole Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        PrimaryButton(text = "Authenticate with Password") {
            store.dispatch(Action.Authenticate(AuthenticationString.Password(password)))
        }
        OrDivider()
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .autofill(listOf(AutofillType.Password), onFill = setApiKey),
            value = apiKey,
            onValueChange = setApiKey,
            label = { Text("Pi-hole API Key") },
            visualTransformation = PasswordVisualTransformation(),
        )
        PrimaryButton(text = "Authenticate with API Key") {
            coroutineScope.launch {
                store.dispatch(Action.Authenticate(AuthenticationString.Token(apiKey)))
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.autofill(
    autofillTypes: List<AutofillType>,
    onFill: ((String) -> Unit),
) = composed {
    val autofill = LocalAutofill.current
    val autofillNode = AutofillNode(onFill = onFill, autofillTypes = autofillTypes)
    LocalAutofillTree.current += autofillNode

    this
        .onGloballyPositioned {
            autofillNode.boundingBox = it.boundsInWindow()
        }
        .onFocusChanged { focusState ->
            autofill?.run {
                if (focusState.isFocused) {
                    requestAutofillForNode(autofillNode)
                } else {
                    cancelAutofillForNode(autofillNode)
                }
            }
        }
}