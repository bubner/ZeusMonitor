package me.bubner.zeusmonitor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.bubner.zeusmonitor.timer.ElapsedTime
import me.bubner.zeusmonitor.ui.CalculatingText
import me.bubner.zeusmonitor.ui.ControlButton
import me.bubner.zeusmonitor.ui.LiveTimer
import me.bubner.zeusmonitor.ui.Result
import me.bubner.zeusmonitor.ui.StopButton
import me.bubner.zeusmonitor.util.CenteredColumn
import kotlin.time.Duration

@Preview
@Composable
fun MainScreen(onNewItem: (Duration) -> Unit = {}) {
    val timer = rememberSaveable(saver = ElapsedTime.saver) { ElapsedTime() }
    var isActive by rememberSaveable { mutableStateOf(false) }
    var needReset by remember { mutableStateOf(false) }

    LaunchedEffect(isActive) {
        if (isActive) timer.run()
        if (needReset) {
            timer.reset()
            @Suppress("AssignedValueIsNeverRead")
            needReset = false
        } else if (!isActive && timer.isValid) {
            onNewItem(timer.elapsedTime)
        }
    }

    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        CenteredColumn(Modifier.padding(24.dp)) {
            CenteredColumn(Modifier.padding(12.dp)) {
                CenteredColumn {
                    AnimatedVisibility(
                        visible = isActive,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        CalculatingText()
                    }
                    Result(isActive, timer)
                }
                LiveTimer(isActive, timer)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = isActive,
                    enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                    exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
                ) {
                    StopButton {
                        isActive = false
                        needReset = true
                    }
                }
                ControlButton(isActive) {
                    isActive = !isActive
                    if (isActive)
                        timer.reset()
                }
            }
        }
    }
}