@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.wbrawner.pihelper.shared.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.smallTopAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wbrawner.pihelper.shared.Action
import com.wbrawner.pihelper.shared.Effect
import com.wbrawner.pihelper.shared.Status
import com.wbrawner.pihelper.shared.Store
import com.wbrawner.pihelper.shared.ui.component.LoadingSpinner
import com.wbrawner.pihelper.shared.ui.component.PrimaryButton
import kotlin.math.pow
import kotlin.math.roundToLong
import com.wbrawner.pihelper.shared.State as PihelperState

const val MAIN_SCREEN_TAG = "mainScreen"
const val STATUS_TEXT_TAG = "statusText"
const val ENABLE_BUTTON_TAG = "enableButton"
const val DISABLE_TEN_BUTTON_TAG = "disableTenButton"
const val DISABLE_THIRTY_BUTTON_TAG = "disableThirtyButton"
const val DISABLE_FIVE_BUTTON_TAG = "disableFiveButton"
const val DISABLE_CUSTOM_BUTTON_TAG = "disableCustomButton"
const val DISABLE_CUSTOM_INPUT_TAG = "disableCustomInput"
const val DISABLE_CUSTOM_INPUT_SECONDS_TAG = "disableCustomInputSeconds"
const val DISABLE_CUSTOM_INPUT_MINUTES_TAG = "disableCustomInputMinutes"
const val DISABLE_CUSTOM_INPUT_HOURS_TAG = "disableCustomInputHours"
const val DISABLE_CUSTOM_CANCEL_BUTTON_TAG = "disableCustomCancelButton"
const val DISABLE_CUSTOM_SUBMIT_BUTTON_TAG = "disableCustomSubmitButton"
const val DISABLE_PERMANENT_BUTTON_TAG = "disablePermanentButton"

@ExperimentalAnimationApi
@Composable
fun MainScreen(store: Store) {
    val state by store.state.collectAsState()
    val effect by store.effects.collectAsState(initial = Effect.Empty)
    MainScreen(state = state, error = effect as? Effect.Error, dispatch = store::dispatch)
}

@ExperimentalAnimationApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: PihelperState,
    error: Effect.Error? = null,
    dispatch: (Action) -> Unit
) {

    Scaffold(
        modifier = Modifier.testTag(MAIN_SCREEN_TAG),
        topBar = {
            TopAppBar(
                title = { Text("Pi-helper") },
                colors = smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = { dispatch(Action.About) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val status = state.status
            LoadingSpinner(state.loading)
            if (status != null) {
                val enabled = status is Status.Enabled
                StatusLabel(status)
                AnimatedContent(targetState = enabled, contentAlignment = Alignment.Center) {
                    if (enabled) {
                        DisableControls { duration -> dispatch(Action.Disable(duration)) }
                    } else {
                        EnableControls { dispatch(Action.Enable) }
                    }
                }
            }
            error?.let {
                Text(
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    text = "${it.message}"
                )
            }
        }
    }
}

@Composable
fun StatusLabel(status: Status) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(color = MaterialTheme.colorScheme.onSurface, text = "Status:")
        Spacer(modifier = Modifier.width(8.dp))
        val color = when (status) {
            is Status.Enabled -> MaterialTheme.colorScheme.secondary
            is Status.Disabled -> MaterialTheme.colorScheme.primary
            else -> Color(0x00000000)
        }
        Text(
            modifier = Modifier.testTag(STATUS_TEXT_TAG),
            color = color,
            fontWeight = FontWeight.Bold,
            text = status.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        )
        if (status is Status.Disabled && !status.timeRemaining.isNullOrBlank()) {
            Text(
                color = color,
                fontWeight = FontWeight.Bold,
                text = " (${status.timeRemaining})"
            )
        }
    }
}

@Composable
fun EnableControls(onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .testTag(ENABLE_BUTTON_TAG)
            .fillMaxWidth()
            .padding(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        onClick = onClick
    ) {
        Text("Enable")
    }
}

@Composable
fun DisableControls(disable: (duration: Long?) -> Unit) {
    val (dialogVisible, setDialogVisible) = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        PrimaryButton(
            modifier = Modifier.testTag(DISABLE_TEN_BUTTON_TAG),
            text = "Disable for 10 seconds"
        ) { disable(10) }
        PrimaryButton(
            modifier = Modifier.testTag(DISABLE_THIRTY_BUTTON_TAG),
            text = "Disable for 30 seconds"
        ) { disable(30) }
        PrimaryButton(
            modifier = Modifier.testTag(DISABLE_FIVE_BUTTON_TAG),
            text = "Disable for 5 minutes"
        ) { disable(300) }
        PrimaryButton(
            modifier = Modifier.testTag(DISABLE_CUSTOM_BUTTON_TAG),
            text = "Disable for custom time"
        ) { setDialogVisible(true) }
        PrimaryButton(
            modifier = Modifier.testTag(DISABLE_PERMANENT_BUTTON_TAG),
            text = "Disable permanently"
        ) { disable(null) }
        CustomTimeDialog(dialogVisible, setDialogVisible) {
            disable(it)
        }
    }
}

enum class Duration {
    SECONDS,
    MINUTES,
    HOURS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTimeDialog(
    visible: Boolean,
    setVisible: (Boolean) -> Unit,
    onTimeSelected: (Long) -> Unit
) {
    if (!visible) return
    val (time: String, setTime: (String) -> Unit) = remember { mutableStateOf("10") }
    val (duration, selectDuration: (Duration) -> Unit) = remember {
        mutableStateOf(Duration.SECONDS)
    }
    AlertDialog(
        shape = MaterialTheme.shapes.small,
        onDismissRequest = { setVisible(false) },
        dismissButton = {
            TextButton(
                modifier = Modifier.testTag(DISABLE_CUSTOM_CANCEL_BUTTON_TAG),
                onClick = { setVisible(false) }
            ) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.testTag(DISABLE_CUSTOM_SUBMIT_BUTTON_TAG),
                onClick = {
                    // TODO: Move this math to the store
                    onTimeSelected(time.toLong() * (60.0.pow(duration.ordinal)).roundToLong())
                    setVisible(false)
                }
            ) {
                Text("Disable")
            }
        },
        title = { Text("Disable for custom time:") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    modifier = Modifier.testTag(DISABLE_CUSTOM_INPUT_TAG),
                    value = time,
                    onValueChange = { setTime(it) },
                    placeholder = { Text("Time to disable") }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    DurationToggle(
                        modifier = Modifier.testTag(DISABLE_CUSTOM_INPUT_SECONDS_TAG),
                        selected = duration == Duration.SECONDS,
                        onClick = { selectDuration(Duration.SECONDS) },
                        text = "Secs"
                    )
                    DurationToggle(
                        modifier = Modifier.testTag(DISABLE_CUSTOM_INPUT_MINUTES_TAG),
                        selected = duration == Duration.MINUTES,
                        onClick = { selectDuration(Duration.MINUTES) },
                        text = "Mins"
                    )
                    DurationToggle(
                        modifier = Modifier.testTag(DISABLE_CUSTOM_INPUT_HOURS_TAG),
                        selected = duration == Duration.HOURS,
                        onClick = { selectDuration(Duration.HOURS) },
                        text = "Hours"
                    )
                }
            }
        }
    )
}

@Composable
fun DurationToggle(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    text: String
) {
    Row(
        modifier = modifier.selectable(selected = selected, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.background,
                contentColor = if (selected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.primary
            ),
            elevation = null
        ) {
            Text(text = text)
        }
    }
}

//@Composable
//@DayNightPreview
//fun CustomTimeDialog_Preview() {
//    PihelperTheme {
//        CustomTimeDialog(true, {}) { }
//    }
//}
//
//@Composable
//@DayNightPreview
//fun StatusLabelEnabled_Preview() {
//    PihelperTheme {
//        StatusLabel(Status.Enabled)
//    }
//}
//
//@Composable
//@DayNightPreview
//fun StatusLabelDisabled_Preview() {
//    PihelperTheme {
//        StatusLabel(Status.Disabled())
//    }
//}
//
//@Composable
//@DayNightPreview
//fun StatusLabelDisabledWithTime_Preview() {
//    PihelperTheme {
//        StatusLabel(Status.Disabled("12:34:56"))
//    }
//}
//
//@Composable
//@DayNightPreview
//fun PrimaryButton_Preview() {
//    PihelperTheme {
//        PrimaryButton(text = "Disable") {}
//    }
//}
//
//@Composable
//@DayNightPreview
//fun EnableControls_Preview() {
//    PihelperTheme {
//        EnableControls {}
//    }
//}
//
//@Composable
//@DayNightPreview
//fun DisableControls_Preview() {
//    PihelperTheme {
//        DisableControls {}
//    }
//}
