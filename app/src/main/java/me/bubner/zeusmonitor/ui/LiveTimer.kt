package me.bubner.zeusmonitor.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.bubner.zeusmonitor.timer.ElapsedTime
import me.bubner.zeusmonitor.util.Math.round
import kotlin.time.DurationUnit

@Preview
@Composable
fun LiveTimer(active: Boolean = false, timer: ElapsedTime = ElapsedTime()) {
    val fontSize by animateDpAsState(
        targetValue = if (active) 48.dp else 24.dp,
        label = "timer size"
    )

    Text(
        text = "${timer.elapsedTime.toDouble(DurationUnit.SECONDS) round 2} s",
        style = MaterialTheme.typography.displayLarge.copy(fontSize = fontSize.value.sp)
    )
}
