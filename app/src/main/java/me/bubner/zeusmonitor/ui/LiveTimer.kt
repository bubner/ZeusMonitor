package me.bubner.zeusmonitor.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.bubner.zeusmonitor.timer.ElapsedTime
import me.bubner.zeusmonitor.util.CenteredColumn
import me.bubner.zeusmonitor.util.Math.lerp
import me.bubner.zeusmonitor.util.Math.round
import me.bubner.zeusmonitor.util.pad
import kotlin.time.DurationUnit

@Preview(showBackground = true, backgroundColor = 0xffffff)
@Composable
fun LiveTimer(active: Boolean = false, timer: ElapsedTime = ElapsedTime()) {
    val fontSize by animateDpAsState(
        targetValue = if (active) 48.dp else 24.dp,
    )

    var size by remember { mutableStateOf(IntSize.Zero) }
    val t = if (active) {
        val transition = rememberInfiniteTransition()
        val anim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
        )
        anim
    } else {
        0.0f
    }

    CenteredColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .onSizeChanged { size = it }
            .drawBehind {
                if (!active)
                    return@drawBehind
                val width = size.width.toFloat()
                val height = size.height.toFloat()
                val gradientWidth = width * 0.35f
                // t runs from 0 .. 1, so lerp between -gradientWidth to width
                // this moves the position of the Yellow brush color between each end
                val startX = -gradientWidth..width lerp t
                val brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Yellow.copy(alpha = 0.7f),
                        Color.Transparent
                    ),
                    start = Offset(startX, 0f),
                    end = Offset(startX + gradientWidth, 0f)
                )
                drawRect(brush = brush, size = Size(width, height))
            }
    ) {
        Text(
            text = "${timer.elapsedTime.toDouble(DurationUnit.SECONDS) round 2 pad 2} s",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = fontSize.value.sp),
        )
    }
}