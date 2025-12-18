package me.bubner.zeusmonitor.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ControlButton(active: Boolean, onClick: () -> Unit) {
    val buttonColour by animateColorAsState(
        targetValue = if (active)
            MaterialTheme.colorScheme.secondary
        else
            MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 250),
        label = "button color"
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
        if (active) {
            Text("Heard Thunder")
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Heard Thunder",
                Modifier.padding(4.dp)
            )
        } else {
            Text("Saw Flash")
            Icon(imageVector = Icons.Default.Bolt, contentDescription = "Saw Flash")
        }
    }
}

@Preview
@Composable
fun ControlButtonPreview() {
    ControlButton(false) {}
}