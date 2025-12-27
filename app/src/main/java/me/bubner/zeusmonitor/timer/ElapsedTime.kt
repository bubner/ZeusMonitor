package me.bubner.zeusmonitor.timer

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
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