@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.wbrawner.pihelper

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wbrawner.pihelper.shared.Action
import com.wbrawner.pihelper.shared.Status
import com.wbrawner.pihelper.shared.Store
import com.wbrawner.pihelper.ui.DayNightPreview
import com.wbrawner.pihelper.ui.PihelperTheme
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToLong

@ExperimentalAnimationApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(store: Store) {
    val state = store.state.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pi-helper") },
                colors = smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = { store.dispatch(Action.About) }) {
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
            val status = state.value.status
            LoadingSpinner(state.value.loading)
            if (status != null) {
                val enabled = status is Status.Enabled
                StatusLabel(status)
                AnimatedContent(targetState = enabled, contentAlignment = Alignment.Center) {
                    if (enabled) {
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
            color = color,
            fontWeight = FontWeight.Bold,
            text = status.name.capitalize(Locale.US)
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
        PrimaryButton(text = "Disable for 10 seconds") { disable(10) }
        PrimaryButton(text = "Disable for 30 seconds") { disable(30) }
        PrimaryButton(text = "Disable for 5 minutes") { disable(300) }
        PrimaryButton(text = "Disable for custom time") { setDialogVisible(true) }
        PrimaryButton(text = "Disable permanently") { disable(null) }
        CustomTimeDialog(dialogVisible, setDialogVisible) {
            disable(it)
        }
    }
}

@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
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
        dismissButton = {
            TextButton({ setVisible(false) }) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // TODO: Move this math to the viewmodel or repository
                onTimeSelected(time.toLong() * (60.0.pow(duration.ordinal)).roundToLong())
                setVisible(false)
            }) {
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
                    value = time,
                    onValueChange = { setTime(it) },
                    placeholder = { Text("Time to disable") }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    DurationToggle(
                        selected = duration == Duration.SECONDS,
                        onClick = { selectDuration(Duration.SECONDS) },
                        text = "Secs"
                    )
                    DurationToggle(
                        selected = duration == Duration.MINUTES,
                        onClick = { selectDuration(Duration.MINUTES) },
                        text = "Mins"
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

@Composable
@DayNightPreview
fun CustomTimeDialog_Preview() {
    PihelperTheme {
        CustomTimeDialog(true, {}) { }
    }
}

@Composable
@DayNightPreview
fun StatusLabelEnabled_Preview() {
    PihelperTheme {
        StatusLabel(Status.Enabled)
    }
}

@Composable
@DayNightPreview
fun StatusLabelDisabled_Preview() {
    PihelperTheme {
        StatusLabel(Status.Disabled())
    }
}

@Composable
@DayNightPreview
fun StatusLabelDisabledWithTime_Preview() {
    PihelperTheme {
        StatusLabel(Status.Disabled("12:34:56"))
    }
}

@Composable
@DayNightPreview
fun PrimaryButton_Preview() {
    PihelperTheme {
        PrimaryButton(text = "Disable") {}
    }
}

@Composable
@DayNightPreview
fun EnableControls_Preview() {
    PihelperTheme {
        EnableControls {}
    }
}

@Composable
@DayNightPreview
fun DisableControls_Preview() {
    PihelperTheme {
        DisableControls {}
    }
}
