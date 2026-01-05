package me.bubner.zeusmonitor.timer

import kotlinx.serialization.Serializable
import me.bubner.zeusmonitor.util.serialize
import org.maplibre.android.geometry.LatLng
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Serializable
data class HistoryItem(
    val elapsedTimeSec: Double,
    val distanceKm: Double,
    val speedOfSoundMPerS: Double,
    val latLng: Pair<Double, Double>,
    val unixTimeMillis: Long = System.currentTimeMillis()
) {
    constructor(
        elapsedTime: Duration,
        distanceKm: Double,
        speedOfSoundMPers: Double,
        latLng: LatLng
    )
            : this(
        elapsedTime.toDouble(DurationUnit.SECONDS),
        distanceKm,
        speedOfSoundMPers,
        latLng.serialize()
    )
}
