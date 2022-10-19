package com.wbrawner.pihelper

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wbrawner.pihelper.shared.Action
import com.wbrawner.pihelper.shared.Status
import com.wbrawner.pihelper.shared.Store
import com.wbrawner.pihelper.ui.PihelperTheme
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToLong

@ExperimentalAnimationApi
@Composable
fun MainScreen(store: Store) {
    val state = store.state.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pi-helper") },
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                elevation = 0.dp,
                actions = {
                    IconButton(onClick = { store.dispatch(Action.About) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colors.onBackground
                        )
                    }
                }
            )
        }
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoadingSpinner(state.value.loading)
            AnimatedContent(targetState = state.value.status, contentAlignment = Alignment.Center) {
                state.value.status?.let {
                    StatusLabel(it)
                    if (it == Status.ENABLED) {
                        DisableControls { duration -> store.dispatch(Action.Disable(duration)) }
                    } else {
                        EnableControls { store.dispatch(Action.Enable) }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusLabel(status: Status) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(color = MaterialTheme.colors.onSurface, text = "Status:")
        Spacer(modifier = Modifier.width(8.dp))
        val color = when (status) {
            Status.ENABLED -> MaterialTheme.colors.secondaryVariant
            Status.DISABLED -> MaterialTheme.colors.primaryVariant
            else -> Color(0x00000000)
        }
        Text(
            color = color,
            fontWeight = FontWeight.Bold,
            text = status.name.toLowerCase(Locale.US).capitalize(Locale.US)
        )
    }
}

@Composable
fun EnableControls(onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            // The strange padding is to work around a bug with the animation
            .padding(start = 16.dp, top = 48.dp, end = 16.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.secondary,
            contentColor = MaterialTheme.colors.onSecondary
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
            // The strange padding is to work around a bug with the animation
            .padding(start = 16.dp, top = 48.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        PrimaryButton("Disable for 10 seconds") { disable(10) }
        PrimaryButton("Disable for 30 seconds") { disable(30) }
        PrimaryButton("Disable for 5 minutes") { disable(300) }
        PrimaryButton("Disable for custom time") { setDialogVisible(true) }
        PrimaryButton("Disable permanently") { disable(null) }
        CustomTimeDialog(dialogVisible, setDialogVisible) {
            disable(it)
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary
        ),
        onClick = onClick
    ) {
        Text(text)
    }
}

enum class Duration {
    SECONDS,
    MINUTES,
    HOURS
}

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
        buttons = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton({ setVisible(false) }) {
                    Text("Cancel")
                }
                TextButton(onClick = {
                    // TODO: Move this math to the viewmodel or repository
                    onTimeSelected(time.toLong() * (60.0.pow(duration.ordinal)).roundToLong())
                    setVisible(false)
                }) {
                    Text("Disable")
                }
            }
        },
        title = { Text("Disable for custom time: ") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = time,
                    onValueChange = { setTime(it) },
                    placeholder = { Text("Time to disable") }
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DurationToggle(
                        selected = duration == Duration.SECONDS,
                        onClick = { selectDuration(Duration.SECONDS) },
                        text = "Seconds"
                    )
                    DurationToggle(
                        selected = duration == Duration.MINUTES,
                        onClick = { selectDuration(Duration.MINUTES) },
                        text = "Minutes"
                    )
                    DurationToggle(
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
fun DurationToggle(selected: Boolean, onClick: () -> Unit, text: String) {
    Row(
        modifier = Modifier.selectable(selected = selected, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (selected)
                    MaterialTheme.colors.primary
                else
                    MaterialTheme.colors.background,
                contentColor = if (selected)
                    MaterialTheme.colors.onPrimary
                else
                    MaterialTheme.colors.primary
            ),
            elevation = null
        ) {
            Text(text = text)
        }
    }
}

@Composable
@Preview
fun CustomTimeDialog_Preview() {
    CustomTimeDialog(true, {}) { }
}

@Composable
@Preview
fun StatusLabelEnabled_Preview() {
    PihelperTheme(false) {
        StatusLabel(Status.ENABLED)
    }
}

@Composable
@Preview
fun StatusLabelEnabled_DarkPreview() {
    PihelperTheme(true) {
        StatusLabel(Status.ENABLED)
    }
}

@Composable
@Preview
fun StatusLabelDisabled_Preview() {
    PihelperTheme(false) {
        StatusLabel(Status.DISABLED)
    }
}

@Composable
@Preview
fun StatusLabelDisabled_DarkPreview() {
    PihelperTheme(true) {
        StatusLabel(Status.DISABLED)
    }
}

@Composable
@Preview
fun PrimaryButton_Preview() {
    PihelperTheme(false) {
        PrimaryButton("Disable") {}
    }
}

@Composable
@Preview
fun PrimaryButton_DarkPreview() {
    PihelperTheme(true) {
        PrimaryButton("Disable") {}
    }
}

@Composable
@Preview
fun EnableControls_Preview() {
    PihelperTheme(false) {
        EnableControls {}
    }
}

@Composable
@Preview
fun EnableControls_DarkPreview() {
    PihelperTheme(true) {
        EnableControls {}
    }
}

@Composable
@Preview
fun DisableControls_Preview() {
    DisableControls({})
}
