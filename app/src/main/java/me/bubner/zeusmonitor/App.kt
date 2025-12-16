package me.bubner.zeusmonitor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.bubner.zeusmonitor.timer.ElapsedTime
import me.bubner.zeusmonitor.ui.LiveTimer
import me.bubner.zeusmonitor.util.Math.round
import kotlin.time.DurationUnit

@Preview
@Composable
fun App() {
    var active by remember { mutableStateOf(false) }
    val timer = remember { ElapsedTime() }

    LaunchedEffect(active) {
        if (active) timer.run()
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LiveTimer(timer)
            Button(onClick = { active = !active }) {
                Text(if (active) "Bolt" else "Flash")
            }
            if (!active && timer.isValid)
                Text("Lightning is ${timer.elapsedTime.toDouble(DurationUnit.SECONDS) / 3 round 3} km away.")
        }
    }
}