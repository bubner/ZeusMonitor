package me.bubner.zeusmonitor.util

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import org.maplibre.android.geometry.LatLng

fun Location.toLatLng() = LatLng(latitude, longitude, altitude)

fun String.isPermissionGranted(ctx: Context) =
    ContextCompat.checkSelfPermission(ctx, this) == PackageManager.PERMISSION_GRANTED