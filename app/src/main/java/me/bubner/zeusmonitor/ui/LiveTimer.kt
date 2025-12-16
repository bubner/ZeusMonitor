package me.bubner.zeusmonitor.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.bubner.zeusmonitor.timer.ElapsedTime

@Composable
fun LiveTimer(timer: ElapsedTime) {
    Text(timer.elapsedTime.toString())
}
