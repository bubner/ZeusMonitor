package me.bubner.zeusmonitor.timer

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Serializable
data class HistoryItem(
    val elapsedTimeSec: Double,
    val distanceKm: Double,
    val speedOfSoundMPerS: Double,
    val unixTimeMillis: Long = System.currentTimeMillis()
) {
    constructor(elapsedTime: Duration, distanceKm: Double, speedOfSoundMPers: Double)
            : this(elapsedTime.toDouble(DurationUnit.SECONDS), distanceKm, speedOfSoundMPers)
}
