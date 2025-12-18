package me.bubner.zeusmonitor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun HistoryScreen() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                "History",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(12.dp)
            )
            HorizontalDivider()
        }
        // TODO
    }
}