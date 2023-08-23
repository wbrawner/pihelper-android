package com.wbrawner.pihelper.shared.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wbrawner.pihelper.shared.Action
import com.wbrawner.pihelper.shared.AuthenticationString
import com.wbrawner.pihelper.shared.Effect
import com.wbrawner.pihelper.shared.Store
import com.wbrawner.pihelper.shared.ui.component.LoadingSpinner
import com.wbrawner.pihelper.shared.ui.component.PrimaryButton

const val AUTH_SCREEN_TAG = "authScreen"
const val SUCCESS_TEXT_TAG = "successText"
const val PASSWORD_INPUT_TAG = "passwordInput"
const val PASSWORD_BUTTON_TAG = "passwordButton"
const val API_KEY_INPUT_TAG = "apiKeyInput"
const val API_KEY_BUTTON_TAG = "apiKeyButton"

@Composable
fun AuthScreen(store: Store) {
    val effect by store.effects.collectAsState(initial = Effect.Empty)
    AuthScreen(
        dispatch = store::dispatch,
        effect as? Effect.Error
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    dispatch: (Action) -> Unit,
    error: Effect.Error? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val (password: String, setPassword: (String) -> Unit) = remember { mutableStateOf("") }
    val (apiKey: String, setApiKey: (String) -> Unit) = remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .testTag(AUTH_SCREEN_TAG)
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        LoadingSpinner(animate = false)
        Text(
            modifier = Modifier.testTag(SUCCESS_TEXT_TAG),
            text = "Pi-helper has successfully connected to your Pi-Hole!",
            textAlign = TextAlign.Center
        )
        Text(
            text = "You'll need to authenticate in order to enable and disable the Pi-hole.",
            textAlign = TextAlign.Center
        )
        OutlinedTextField(
            modifier = Modifier
                .testTag(PASSWORD_INPUT_TAG)
                .fillMaxWidth()
                .autofill(listOf(AutofillType.Password), onFill = setPassword),
            value = password,
            onValueChange = setPassword,
            label = { Text("Pi-hole Password") },
            visualTransformation = PasswordVisualTransformation(),
            maxLines = 1
        )
        PrimaryButton(
            modifier = Modifier.testTag(PASSWORD_BUTTON_TAG),
            text = "Authenticate with Password"
        ) {
            keyboardController?.hide()
            dispatch(Action.Authenticate(AuthenticationString.Password(password)))
        }
        OrDivider()
        OutlinedTextField(
            modifier = Modifier
                .testTag(API_KEY_INPUT_TAG)
                .fillMaxWidth()
                .autofill(listOf(AutofillType.Password), onFill = setApiKey),
            value = apiKey,
            onValueChange = setApiKey,
            label = { Text("Pi-hole API Key") },
            visualTransformation = PasswordVisualTransformation(),
            maxLines = 1
        )
        PrimaryButton(
            modifier = Modifier.testTag(API_KEY_BUTTON_TAG),
            text = "Authenticate with API Key"
        ) {
            keyboardController?.hide()
            dispatch(Action.Authenticate(AuthenticationString.Token(apiKey)))
        }
        error?.let {
            Text(
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                text = "Authentication failed: ${it.message}"
            )
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