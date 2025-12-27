package me.bubner.zeusmonitor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.bubner.zeusmonitor.timer.HistoryDataStore
import me.bubner.zeusmonitor.timer.HistoryItem
import kotlin.time.Duration
import kotlin.time.DurationUnit

class ZeusViewModel(app: Application) : AndroidViewModel(app) {
    // TODO: dynamic calculation of this variable and displaying it on the app
    var speedOfSound = 343 // m/s
        private set

    private val dataStore = HistoryDataStore(app.applicationContext)

    fun historyFlow() = dataStore.historyFlow()
    fun deleteAllHistoryItems() = viewModelScope.launch { dataStore.deleteAll() }
    fun deleteHistoryItem(historyItem: HistoryItem) =
        viewModelScope.launch { dataStore.deleteHistoryItem(historyItem) }

    fun onNewItem(duration: Duration) {
        viewModelScope.launch {
            dataStore.pushHistoryItem(HistoryItem(duration, calculateDistanceKm(duration)))
        }
    }

    fun calculateDistanceKm(time: Duration): Double {
        return (time.toDouble(DurationUnit.SECONDS) * speedOfSound) / 1000
    }
}