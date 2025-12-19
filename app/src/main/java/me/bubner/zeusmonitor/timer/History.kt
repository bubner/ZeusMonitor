package me.bubner.zeusmonitor.timer

import kotlinx.serialization.Serializable

@Serializable
data class History(
    val history: List<HistoryItem>
)