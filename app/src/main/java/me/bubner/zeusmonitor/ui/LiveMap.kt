package me.bubner.zeusmonitor.ui

import android.location.Location
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.maps.Style
import org.ramani.compose.CameraPosition
import org.ramani.compose.MapLibre
import org.ramani.compose.UiSettings

@Composable
fun ColumnScope.LiveMap(
    modifier: Modifier = Modifier,
    userLocation: Location,
    setUserLocation: (Location) -> Unit
) {
    val location = rememberSaveable { mutableStateOf(userLocation) }
    // TODO: zoom management and rendering expanding circle
    var cameraPosition by rememberSaveable { mutableStateOf(CameraPosition(zoom = 11.0)) }
    LaunchedEffect(location.value) {
        setUserLocation(location.value)
    }

    MapLibre(
        styleBuilder = Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty"),
        modifier = modifier
            .fillMaxSize()
            .weight(1f)
            .clip(RoundedCornerShape(16.dp)),
        userLocation = location,
        cameraMode = remember { mutableIntStateOf(CameraMode.TRACKING_GPS) },
        cameraPosition = cameraPosition,
        uiSettings = UiSettings(
            isLogoEnabled = false,
            isAttributionEnabled = false
        )
    )
}
