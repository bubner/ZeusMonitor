package me.bubner.zeusmonitor.ui

import android.graphics.Color
import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.bubner.zeusmonitor.LOCATION_PENDING_PROVIDER
import me.bubner.zeusmonitor.util.toPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOpacity
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import org.maplibre.spatialk.turf.transformation.circle
import org.maplibre.spatialk.units.extensions.kilometers
import org.ramani.compose.MapLibre
import org.ramani.compose.UiSettings
import org.ramani.compose.rememberMapViewWithLifecycle
import kotlin.math.max

const val MIN_CAM_VIEW_RADIUS_KM = 0.5
const val MIN_CIRCLE_RADIUS_KM = 0.05

@Composable
fun LiveMap(
    userLocation: Location,
    setUserLocation: (Location) -> Unit,
    radiusKm: Double,
    isStopped: Boolean
) {
    val location = rememberSaveable { mutableStateOf(userLocation) }
    LaunchedEffect(location.value) {
        setUserLocation(location.value)
    }

    val mapView = rememberMapViewWithLifecycle()
    val circleFeature = remember(radiusKm, userLocation) {
        val zone = circle(userLocation.toPosition(), max(MIN_CIRCLE_RADIUS_KM, radiusKm).kilometers)
        val bounds = zone.bbox?.let {
            LatLngBounds.from(it.north, it.east, it.south, it.west)
        }
        mapView.getMapAsync {
            if (bounds == null || isStopped)
                return@getMapAsync
            it.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 48))
        }
        Feature.fromGeometry(
            zone.let { circle ->
                Polygon.fromLngLats(listOf(circle.coordinates[0].map {
                    Point.fromLngLat(it.longitude, it.latitude)
                }))
            }
        )
    }

    LaunchedEffect(userLocation.provider, isStopped) {
        if (userLocation.provider == LOCATION_PENDING_PROVIDER && !isStopped)
            return@LaunchedEffect
        val initialBounds =
            circle(userLocation.toPosition(), MIN_CAM_VIEW_RADIUS_KM.kilometers).bbox?.let {
                LatLngBounds.from(it.north, it.east, it.south, it.west)
            }
        if (initialBounds == null)
            return@LaunchedEffect
        mapView.getMapAsync {
            it.moveCamera(CameraUpdateFactory.newLatLngBounds(initialBounds, 48))
        }
    }

    MapLibre(
        mapView = mapView,
        styleBuilder = Style.Builder()
            .fromUri("https://tiles.openfreemap.org/styles/liberty")
            .withLayer(
                FillLayer("circle", "radius").withProperties(
                    fillColor(Color.YELLOW),
                    fillOpacity(if (radiusKm < MIN_CIRCLE_RADIUS_KM || userLocation.provider == LOCATION_PENDING_PROVIDER) 0.0f else 0.5f)
                )
            )
            .withSource(GeoJsonSource("radius", circleFeature)),
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp)),
        userLocation = location,
        uiSettings = UiSettings(
            isLogoEnabled = false,
            isAttributionEnabled = false
        ),
    )
}
