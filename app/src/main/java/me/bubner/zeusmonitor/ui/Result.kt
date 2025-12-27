package me.bubner.zeusmonitor.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.bubner.zeusmonitor.util.Math.round

@Preview
@Composable
fun Result(active: Boolean = false, distance: Double = 0.0) {
    val fontSize by animateDpAsState(
        targetValue = if (active) 24.dp else 48.dp,
        label = "result size"
    )

    Text(
        "${distance round 2} km",
        style = MaterialTheme.typography.displayLarge.copy(fontSize = fontSize.value.sp)
    )
}
