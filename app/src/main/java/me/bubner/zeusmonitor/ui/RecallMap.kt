package me.bubner.zeusmonitor.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.bubner.zeusmonitor.util.invalidLatLng
import me.bubner.zeusmonitor.util.toLatLng
import me.bubner.zeusmonitor.util.toPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.Style
import org.maplibre.spatialk.turf.transformation.circle
import org.maplibre.spatialk.units.extensions.kilometers
import org.ramani.compose.CameraPosition
import org.ramani.compose.Circle
import org.ramani.compose.Fill
import org.ramani.compose.MapLibre
import org.ramani.compose.MapProperties
import org.ramani.compose.UiSettings
import org.ramani.compose.rememberMapViewWithLifecycle
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
fun RecallMap(
    modifier: Modifier = Modifier,
    radiusKm: Double = 0.0,
    latLng: LatLng = invalidLatLng()
) {
    val mapView = rememberMapViewWithLifecycle()
    val zone = circle(latLng.toPosition(), radiusKm.kilometers, (8 * ceil(radiusKm)).roundToInt())
    val bounds = zone.bbox?.let {
        LatLngBounds.from(it.north, it.east, it.south, it.west)
    }
    mapView.getMapAsync {
        if (bounds == null)
            return@getMapAsync
        it.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 48))
    }

    MapLibre(
        styleBuilder = Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty"),
        modifier = modifier.fillMaxSize(),
        cameraPosition = CameraPosition(latLng),
        properties = MapProperties(latLngBounds = bounds),
        uiSettings = UiSettings(
            isLogoEnabled = false,
            isAttributionEnabled = false
        ),
        mapView = mapView,
    ) {
        Fill(
            points = zone.coordinates[0].map { it.toLatLng() },
            fillColor = "Yellow",
            opacity = 0.5f
        )
        Circle(
            center = latLng,
            radius = 8f,
            color = "Red"
        )
    }
}