package me.bubner.zeusmonitor.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.bubner.zeusmonitor.util.CenteredColumn

@Preview
@Composable
fun ControlButton(active: Boolean = false, showWarning: Boolean = false, onClick: () -> Unit = {}) {
    val rawButtonColour = if (active)
        MaterialTheme.colorScheme.secondary
    else
        MaterialTheme.colorScheme.primary
    val buttonColour by animateColorAsState(
        targetValue = rawButtonColour.copy(alpha = if (showWarning) 0.5f else 1.0f),
        animationSpec = tween(durationMillis = 250),
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .size(width = 200.dp, height = 60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColour,
            contentColor = if (active) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
        )
    ) {
        CenteredColumn {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (active) {
                    Text("Heard Thunder")
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Heard Thunder",
                        Modifier.padding(horizontal = 4.dp)
                    )
                } else {
                    Text("Saw Flash")
                    Icon(imageVector = Icons.Default.Bolt, contentDescription = "Saw Flash")
                }
            }
            if (showWarning)
                FlashingText(
                    "Waiting for location...",
                    fontSize = 10.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
        }
    }
}
