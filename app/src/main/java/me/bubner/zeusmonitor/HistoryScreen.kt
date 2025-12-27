package me.bubner.zeusmonitor

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import me.bubner.zeusmonitor.timer.HistoryItem
import me.bubner.zeusmonitor.util.Math.round
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Constant locale is fine, this app shouldn't expect that sort of usage and restarts will resolve
// this synchronisation. Preferred over regenerating `sdf` each render cycle.
@SuppressLint("ConstantLocale")
val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())

@Composable
fun HistoryScreen(history: Flow<List<HistoryItem>>, deleteItem: (HistoryItem) -> Unit) {
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
            items = historyItems.reversed(),
            key = { it.unixTimeMillis }
        ) { item -> Entry(item, onClick = { }) } // TODO: display screen with map? and delete button
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffff)
@Composable
fun Entry(it: HistoryItem = HistoryItem(0.0, 0.0), onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${it.distanceKm round 2} km",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = "${it.elapsedTimeSec round 2} sec",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                    modifier = Modifier.scale(1.5f),
                    contentDescription = "View"
                )
            }
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Text(sdf.format(Date(it.unixTimeMillis)))
            }
        }
    }
}
