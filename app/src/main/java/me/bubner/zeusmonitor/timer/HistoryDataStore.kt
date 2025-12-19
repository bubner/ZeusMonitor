package me.bubner.zeusmonitor.timer

import android.content.Context
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.map

class HistoryDataStore(private val context: Context) {
    val Context.dataStore by dataStore(
        fileName = "history.json",
        serializer = HistorySerializer,
    )

    fun historyFlow() = context.dataStore.data.map { it.history }

    suspend fun pushHistoryItem(historyItem: HistoryItem) {
        context.dataStore.updateData {
            it.copy(history = it.history + historyItem)
        }
    }

    suspend fun deleteHistoryItem(historyItem: HistoryItem) {
        context.dataStore.updateData {
            it.copy(history = it.history.filterNot { item -> item == historyItem })
        }
    }

    suspend fun deleteAll() {
        context.dataStore.updateData {
            it.copy(history = emptyList())
        }
    }
}