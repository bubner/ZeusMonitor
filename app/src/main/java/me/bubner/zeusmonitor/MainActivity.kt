package me.bubner.zeusmonitor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import me.bubner.zeusmonitor.ui.DeleteAllHistoryButton
import me.bubner.zeusmonitor.ui.Navbar
import me.bubner.zeusmonitor.ui.Tab
import me.bubner.zeusmonitor.ui.theme.ZeusMonitorTheme
import me.bubner.zeusmonitor.util.isPermissionGranted

/**
 * Zeus Monitor
 *
 * @author Lucas Bubner, 2025
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!Manifest.permission.ACCESS_FINE_LOCATION.isPermissionGranted(applicationContext) &&
            !Manifest.permission.ACCESS_COARSE_LOCATION.isPermissionGranted(applicationContext)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                0
            )
        }
        setContent { Main() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode != 0)
            return
        ZeusViewModel.locationUnavailable =
            grantResults.all { it == PackageManager.PERMISSION_DENIED }
    }
}

@Preview
@Composable
fun Main(viewModel: ZeusViewModel = viewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val speedOfSound by viewModel.speedOfSound.collectAsState()
    val speedMode by viewModel.speedMode.collectAsState()
    val lastKnownUserSpeedOfSound by viewModel.lastKnownUserSpeedOfSound.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val isFetchingWeather by viewModel.isFetchingWeather.collectAsState()

    // We prefer to use our own colours rather than the user's (yellow/blue)
    ZeusMonitorTheme(dynamicColor = false) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = { Navbar(navController) },
            floatingActionButton = {
                // FAB for History tab
                AnimatedVisibility(
                    visible = currentRoute == Tab.History.name,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    DeleteAllHistoryButton {
                        viewModel.deleteAllHistoryItems()
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Tab.Monitor.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                Tab.entries.forEach { tab ->
                    composable(tab.name) {
                        when (tab) {
                            Tab.Monitor -> MainScreen(
                                fetchResult = viewModel::calculateDistanceKm,
                                onNewItem = viewModel::onNewItem,
                                speedOfSound = speedOfSound,
                                speedMode = speedMode,
                                onWeatherSyncChange = { viewModel.setWeatherSync(it) },
                                onRequestSynchronisation = { viewModel.synchroniseSpeedOfSound() },
                                onUserSpeedOfSoundInput = { viewModel.onUserSpeedOfSoundInput(it) },
                                lastKnownUserSpeedOfSound = lastKnownUserSpeedOfSound,
                                userLocation = userLocation,
                                setUserLocation = { viewModel.updateLocation(it) },
                                isLocationAvailable = viewModel.isLocationAvailable,
                                isFetchingWeather = isFetchingWeather
                            )

                            Tab.History -> HistoryScreen(
                                history = viewModel.historyFlow(),
                                deleteItem = viewModel::deleteHistoryItem
                            )
                        }
                    }
                }
            }
        }

    }
}