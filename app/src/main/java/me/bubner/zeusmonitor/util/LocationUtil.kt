package me.bubner.zeusmonitor.util

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import org.maplibre.android.geometry.LatLng

// altitude is ignored as it is not used
fun Location.toLatLng() = LatLng(latitude, longitude)

fun invalidLatLng() = LatLng(0.0, 181.0)

/**
 * Deserialize from storage a (lat, long) pair. If invalid (e.g. `LatLng(-91.0, 181.0)`), null is returned.
 */
fun Pair<Double, Double>.deserializeLatLng() =
    if (first in -90.0..90.0 && second in -180.0..180.0) LatLng(first, second) else null

/**
 * Serializes latitude and longitude for storage.
 */
fun LatLng.serialize() = latitude to longitude

fun String.isPermissionGranted(ctx: Context) =
    ContextCompat.checkSelfPermission(ctx, this) == PackageManager.PERMISSION_GRANTED