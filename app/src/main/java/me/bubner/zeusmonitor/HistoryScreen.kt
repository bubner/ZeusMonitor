package me.bubner.zeusmonitor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import me.bubner.zeusmonitor.timer.HistoryItem

@Composable
fun HistoryScreen(history: Flow<List<HistoryItem>>) { // TODO: individual delete reqs
    val historyItems by history.collectAsStateWithLifecycle(emptyList())

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                "History",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(12.dp)
            )
            HorizontalDivider()
        }
        items(
            items = historyItems,
            key = { it.unixTimeMillis }
        ) { item -> Entry(item) }
    }
}

@Composable
fun Entry(it: HistoryItem) {
    Text(it.elapsedTimeSec.toString()) // TODO
}
