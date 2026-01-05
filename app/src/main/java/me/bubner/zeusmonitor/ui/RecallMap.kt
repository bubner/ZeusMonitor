package me.bubner.zeusmonitor.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.bubner.zeusmonitor.util.invalidLatLng
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import org.ramani.compose.CameraPosition
import org.ramani.compose.MapLibre
import org.ramani.compose.UiSettings

@Composable
fun RecallMap(modifier: Modifier = Modifier, latLng: LatLng = invalidLatLng()) {
    MapLibre(
        styleBuilder = Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty"),
        modifier = modifier.fillMaxSize(),
        cameraPosition = CameraPosition(latLng, zoom = 10.0), // TODO: zoom
        uiSettings = UiSettings(
            isLogoEnabled = false,
            isAttributionEnabled = false
        )
    )
}