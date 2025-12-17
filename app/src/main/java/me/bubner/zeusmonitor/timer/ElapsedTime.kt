package me.bubner.zeusmonitor.timer

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
    val isValid
        get() = elapsedMs != 0L

    private var elapsedMs by mutableLongStateOf(0L)

    fun reset() {
        elapsedMs = 0
    }

    suspend fun run() {
        val start = SystemClock.elapsedRealtime()
        while (true) {
            elapsedMs = SystemClock.elapsedRealtime() - start
            delay(50)
        }
    }
}