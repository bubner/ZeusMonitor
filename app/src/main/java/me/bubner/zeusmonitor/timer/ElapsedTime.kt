package me.bubner.zeusmonitor.timer

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import me.bubner.zeusmonitor.util.Math.round
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tracks the [elapsedTime] since invoking [run].
 * Does not reset automatically on function completion.
 *
 * @author Lucas Bubner, 2025
 */
class ElapsedTime {
    val elapsedTime
        get() = elapsedMs.milliseconds
    val isValid
        // Simulates an epsilon threshold as we only display 2 dp timer values in seconds
        get() = elapsedMs * 1000 round 2 > 0.0

    private var elapsedMs by mutableLongStateOf(0L)

    fun reset() {
        elapsedMs = 0
    }

    suspend fun run() {
        var last = SystemClock.elapsedRealtime()
        while (true) {
            val now = SystemClock.elapsedRealtime()
            elapsedMs += now - last
            last = now
            delay(50)
        }
    }

    companion object {
        val saver = Saver<ElapsedTime, Long>(
            save = { it.elapsedMs },
            restore = { ElapsedTime().apply { elapsedMs = it } }
        )
    }
}