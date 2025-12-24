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
    private val dataStore = HistoryDataStore(app.applicationContext)

    fun historyFlow() = dataStore.historyFlow()
    fun deleteAllHistoryItems() = viewModelScope.launch { dataStore.deleteAll() }
    fun deleteHistoryItem(historyItem: HistoryItem) =
        viewModelScope.launch { dataStore.deleteHistoryItem(historyItem) }

    fun onNewItem(duration: Duration) {
        // TODO: improve algo and split between history/calc/info getting
        val sec = duration.toDouble(DurationUnit.SECONDS)
        viewModelScope.launch { dataStore.pushHistoryItem(HistoryItem(sec, sec / 3)) }
    }
}