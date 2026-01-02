package me.bubner.zeusmonitor

import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import me.bubner.zeusmonitor.timer.ElapsedTime
import me.bubner.zeusmonitor.ui.ControlButton
import me.bubner.zeusmonitor.ui.LiveMap
import me.bubner.zeusmonitor.ui.LiveTimer
import me.bubner.zeusmonitor.ui.Result
import me.bubner.zeusmonitor.ui.StopButton
import me.bubner.zeusmonitor.ui.theme.Orange
import me.bubner.zeusmonitor.util.CenteredColumn
import me.bubner.zeusmonitor.util.Math.round
import me.bubner.zeusmonitor.util.pad
import kotlin.time.Duration

enum class State {
    STOPPED,
    STARTING,
    RUNNING,
    FINISHING,
    FINISHED
}

@Preview(showBackground = true, backgroundColor = 0xffffff)
@Composable
fun MainScreen(
    onNewItem: (Duration) -> Unit = {},
    fetchResult: (Duration) -> Double = { 0.0 },
    speedOfSound: Double = 0.0,
    speedMode: ZeusViewModel.SpeedMode = ZeusViewModel.SpeedMode.FALLBACK,
    onWeatherSyncChange: (Boolean) -> Unit = {},
    onRequestSynchronisation: () -> Unit = {},
    onUserSpeedOfSoundInput: (Double) -> Unit = {},
    lastKnownUserSpeedOfSound: Double = 0.0,
    userLocation: Location = Location("null"),
    setUserLocation: (Location) -> Unit = {}
) {
    val timer = rememberSaveable(saver = ElapsedTime.saver) { ElapsedTime() }
    var state by rememberSaveable { mutableStateOf(State.STOPPED) }
    var displaySpeedOfSoundInterface by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state) {
        when (state) {
            State.STOPPED -> {
                // Reset but don't record
                timer.reset()
            }

            State.STARTING -> {
                // Reset on start
                timer.reset()
                state = State.RUNNING
            }

            State.RUNNING -> {
                timer.run()
            }

            State.FINISHING -> {
                if (timer.isValid)
                    onNewItem(timer.elapsedTime)
                state = State.FINISHED
            }

            State.FINISHED -> {
                // no-op, we only want to record the time once while preserving the result
                // and this effect can restart
            }
        }
    }

    CenteredColumn {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .clickable { displaySpeedOfSoundInterface = true }
        ) {
            Text(
                text = "Speed of sound in your area: ${speedOfSound round 2 pad 2} m/s",
                textDecoration = TextDecoration.Underline,
                style = MaterialTheme.typography.bodySmall
            )
            Icon(
                imageVector = Icons.Default.FiberManualRecord,
                tint = when (speedMode) {
                    ZeusViewModel.SpeedMode.SYNCHRONISED -> Color.Green
                    ZeusViewModel.SpeedMode.USER -> Color.Cyan
                    ZeusViewModel.SpeedMode.FALLBACK -> Orange
                },
                modifier = Modifier
                    .size(18.dp)
                    .padding(start = 4.dp),
                contentDescription = "Speed of sound status"
            )
        }
        // Lack of recomposition is fine, this will run on initial composition
        if (!ZeusViewModel.locationUnavailable)
            LiveMap(
                modifier = Modifier.padding(horizontal = 16.dp),
                userLocation = userLocation,
                setUserLocation = setUserLocation
            )
        else
            Box(
                modifier = Modifier
                    .background(Color.LightGray)
                    .fillMaxSize()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                Text("Location permission denied. Unable to show map.")
            }
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CenteredColumn(
                modifier = Modifier.padding(bottom = 24.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CenteredColumn(verticalArrangement = Arrangement.spacedBy((-10).dp)) {
                    Result(state == State.RUNNING, fetchResult(timer.elapsedTime))
                    LiveTimer(state == State.RUNNING, timer)
                }
                CenteredColumn {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedVisibility(
                            visible = state == State.RUNNING,
                            enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                            exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
                        ) {
                            StopButton {
                                state = State.STOPPED
                            }
                        }
                        ControlButton(state == State.RUNNING) {
                            state = if (state == State.RUNNING)
                                State.FINISHING
                            else
                                State.STARTING
                        }
                    }
                }
            }
        }
    }

    if (displaySpeedOfSoundInterface)
        SpeedOfSoundEditor(
            onDismissRequest = {
                @Suppress("AssignedValueIsNeverRead")
                displaySpeedOfSoundInterface = false
            },
            speedOfSound = speedOfSound,
            speedMode = speedMode,
            onWeatherSyncChange = onWeatherSyncChange,
            onRequestSynchronisation = onRequestSynchronisation,
            onUserSpeedOfSoundInput = onUserSpeedOfSoundInput,
            lastKnownUserSpeedOfSound = lastKnownUserSpeedOfSound
        )
}

private val ACCEPTED_INPUT_RANGE = 0.0..10000.0

@Preview
@Composable
fun SpeedOfSoundEditor(
    onDismissRequest: () -> Unit = {},
    speedOfSound: Double = 0.0,
    speedMode: ZeusViewModel.SpeedMode = ZeusViewModel.SpeedMode.USER,
    onWeatherSyncChange: (Boolean) -> Unit = {},
    onRequestSynchronisation: () -> Unit = {},
    onUserSpeedOfSoundInput: (Double) -> Unit = {},
    lastKnownUserSpeedOfSound: Double = 0.0
) {
    var useWeatherSync by remember { mutableStateOf(speedMode != ZeusViewModel.SpeedMode.USER) }
    val userInput = rememberTextFieldState(lastKnownUserSpeedOfSound round 2 pad 2)
    LaunchedEffect(userInput.text) {
        // Will only accept valid decimals
        userInput.text.toString().toDoubleOrNull()?.let {
            if (it in ACCEPTED_INPUT_RANGE)
                onUserSpeedOfSoundInput(it)
        }
    }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CenteredColumn(modifier = Modifier.padding(12.dp)) {
                    Text("Speed of sound")
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            speedOfSound round 2 pad 2,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Text("metres/sec", modifier = Modifier.padding(bottom = 6.dp, start = 6.dp))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FiberManualRecord,
                            tint = when (speedMode) {
                                ZeusViewModel.SpeedMode.SYNCHRONISED -> Color.Green
                                ZeusViewModel.SpeedMode.USER -> Color.Cyan
                                ZeusViewModel.SpeedMode.FALLBACK -> Orange
                            },
                            modifier = Modifier
                                .size(24.dp)
                                .padding(start = 4.dp),
                            contentDescription = "Speed of sound status"
                        )
                        Text(
                            text = when (speedMode) {
                                ZeusViewModel.SpeedMode.SYNCHRONISED -> "Synchronised with weather"
                                ZeusViewModel.SpeedMode.USER -> "Using user-supplied speed"
                                ZeusViewModel.SpeedMode.FALLBACK -> "Waiting for weather sync"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                CenteredColumn {
                    Text("Use weather synchronisation?")
                    Switch(checked = useWeatherSync, onCheckedChange = {
                        useWeatherSync = it
                        onWeatherSyncChange(it)
                        // Auto input or auto sync
                        if (!it)
                            onUserSpeedOfSoundInput(lastKnownUserSpeedOfSound)
                        else
                            onRequestSynchronisation()
                    })
                }
                if (useWeatherSync) {
                    Button(
                        onClick = {
                            onRequestSynchronisation()
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.secondary,
                            containerColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text("Request synchronisation")
                    }
                } else {
                    OutlinedTextField(
                        state = userInput,
                        label = { Text("Set speed of sound (m/s)") },
                        lineLimits = TextFieldLineLimits.SingleLine,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        inputTransformation = InputTransformation {
                            asCharSequence().toString().toDoubleOrNull()?.let {
                                if (it !in ACCEPTED_INPUT_RANGE)
                                    revertAllChanges()
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedLabelColor = MaterialTheme.colorScheme.secondary,
                            focusedIndicatorColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Close", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}
