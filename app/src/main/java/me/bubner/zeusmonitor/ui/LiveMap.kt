package me.bubner.zeusmonitor.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.maplibre.android.maps.Style
import org.ramani.compose.MapLibre

@Composable
fun ColumnScope.LiveMap(modifier: Modifier = Modifier) {
    MapLibre(
        styleBuilder = Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty"),
        modifier = modifier
            .fillMaxSize()
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
    )
}
