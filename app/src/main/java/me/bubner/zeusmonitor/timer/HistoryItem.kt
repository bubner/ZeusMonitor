package me.bubner.zeusmonitor.timer

import kotlinx.serialization.Serializable

@Serializable
data class HistoryItem(
    val elapsedTimeSec: Double,
    val distanceKm: Double,
    val unixTimeMillis: Long = System.currentTimeMillis()
)
