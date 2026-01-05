package me.bubner.zeusmonitor.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.bubner.zeusmonitor.ZeusViewModel
import me.bubner.zeusmonitor.ui.theme.Orange

@Composable
fun StatusIcon(
    speedMode: ZeusViewModel.SpeedMode,
    shouldPulsate: Boolean = false,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
        ),
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
        ),
    )

    val tint = when (speedMode) {
        ZeusViewModel.SpeedMode.SYNCHRONISED -> Color.Green
        ZeusViewModel.SpeedMode.USER -> Color.Cyan
        ZeusViewModel.SpeedMode.FALLBACK -> Orange
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
        if (shouldPulsate) {
            Icon(
                imageVector = Icons.Default.FiberManualRecord,
                tint = tint,
                modifier = Modifier
                    .size((18 * scale).dp)
                    .alpha(alpha),
                contentDescription = null
            )
        }
        Icon(
            imageVector = Icons.Default.FiberManualRecord,
            tint = tint,
            modifier = Modifier
                .size(18.dp),
            contentDescription = "Speed of sound status"
        )
    }
}