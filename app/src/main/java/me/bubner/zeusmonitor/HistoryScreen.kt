package me.bubner.zeusmonitor

import android.annotation.SuppressLint
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import me.bubner.zeusmonitor.timer.HistoryItem
import me.bubner.zeusmonitor.util.Math.round
import me.bubner.zeusmonitor.util.pad
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Constant locale is fine, this app shouldn't expect that sort of usage and restarts will resolve
// this synchronisation. Preferred over regenerating `sdf` each render cycle.
@SuppressLint("ConstantLocale")
val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a z", Locale.getDefault())

@Composable
fun HistoryScreen(history: Flow<List<HistoryItem>>, deleteItem: (HistoryItem) -> Unit) {
    val historyItems by history.collectAsStateWithLifecycle(emptyList())
    var dialog by remember { mutableStateOf(@Composable {}) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
    ) {
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
        ) { item ->
            Entry(
                item,
                onClick = {
                    dialog = @Composable {
                        ItemDialog(
                            item,
                            onDismissRequest = { dialog = {} },
                            onDelete = {
                                deleteItem(item)
                                dialog = {}
                            }
                        )
                    }
                })
        }
    }

    dialog()
}

@Preview(showBackground = true, backgroundColor = 0xffffff)
@Composable
fun Entry(it: HistoryItem = HistoryItem(0.0, 0.0, 0.0), onClick: () -> Unit = {}) {
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
                    text = "${it.distanceKm round 2 pad 2} km",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = "${it.elapsedTimeSec round 2 pad 2} sec",
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
                Text(
                    sdf.format(Date(it.unixTimeMillis)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview
@Composable
fun ItemDialog(
    it: HistoryItem = HistoryItem(0.0, 0.0, 0.0),
    onDismissRequest: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    sdf.format(it.unixTimeMillis),
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
                Text("Lightning estimated to be")
                Text(
                    "${it.distanceKm round 2 pad 2} km",
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.headlineLarge
                )
                Text("from your location at this time.")
                // TODO: use a history map that doesnt use user loc but instead store lat/lon
//                LiveMap(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    "(timed ${it.elapsedTimeSec round 2 pad 2} sec at ${it.speedOfSoundMPerS round 2 pad 2} m/s)",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.padding(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                            containerColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Delete")
                        Icon(
                            imageVector = Icons.Default.Delete,
                            modifier = Modifier.scale(0.8f),
                            contentDescription = "Delete"
                        )
                    }
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Dismiss", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}
