package me.bubner.zeusmonitor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.bubner.zeusmonitor.timer.ElapsedTime
import me.bubner.zeusmonitor.ui.ControlButton
import me.bubner.zeusmonitor.ui.LiveTimer
import me.bubner.zeusmonitor.ui.Result
import me.bubner.zeusmonitor.ui.StopButton
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
    speedOfSound: Double = 0.0
) {
    val timer = rememberSaveable(saver = ElapsedTime.saver) { ElapsedTime() }
    var state by rememberSaveable { mutableStateOf(State.STOPPED) }

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

    // TODO: map and speed of sound display/editor system
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
            .clickable {//todo
            }
    ) {
        Text(
            text = "Speed of sound in your area: ${speedOfSound round 2 pad 2} m/s",
            textDecoration = TextDecoration.Underline,
            style = MaterialTheme.typography.bodySmall
        )
        Icon(
            imageVector = Icons.Default.FiberManualRecord,
            tint = Color.Green,
            modifier = Modifier
                .size(18.dp)
                .padding(start = 4.dp),
            contentDescription = ""
        )
    }

    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        CenteredColumn(
            modifier = Modifier.padding(24.dp),
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