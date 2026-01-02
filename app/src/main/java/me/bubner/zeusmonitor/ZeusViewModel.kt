package me.bubner.zeusmonitor

import android.app.Application
import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bubner.zeusmonitor.timer.HistoryDataStore
import me.bubner.zeusmonitor.timer.HistoryItem
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.DurationUnit

private const val SETTINGS_WEATHER_SYNC_ENABLED = "weatherSyncEnabled"
private const val SETTINGS_LAST_USER_SPEED_OF_SOUND = "lastUserSpeedOfSound"
private const val LOCATION_PENDING_PROVIDER = "null"

class ZeusViewModel(app: Application) : AndroidViewModel(app) {
    private val sharedPrefs = app.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _lastKnownUserSpeedOfSound = MutableStateFlow(
        sharedPrefs.getFloat(SETTINGS_LAST_USER_SPEED_OF_SOUND, 343f).toDouble()
    )
    private val _speedOfSound = MutableStateFlow(_lastKnownUserSpeedOfSound.value) // m/s
    private val _speedMode = MutableStateFlow(SpeedMode.FALLBACK)
    private val _userLocation = MutableStateFlow(Location(LOCATION_PENDING_PROVIDER))

    /**
     * Speed of sound as defined by the current [speedMode].
     */
    val speedOfSound: StateFlow<Double> = _speedOfSound

    /**
     * The method used for fetching the current speed of sound.
     */
    val speedMode: StateFlow<SpeedMode> = _speedMode

    /**
     * The last known speed of sound set by the user.
     */
    val lastKnownUserSpeedOfSound: StateFlow<Double> = _lastKnownUserSpeedOfSound

    /**
     * Last known mutable user location. Updated by MapLibre via [updateLocation].
     */
    val userLocation: StateFlow<Location> = _userLocation

    private val dataStore = HistoryDataStore(app.applicationContext)

    init {
        invalidateAndRefreshMode()
        synchroniseSpeedOfSound()
    }

    fun setWeatherSync(syncEnabled: Boolean) {
        // Use synchronous write to ensure mode state does not desync
        sharedPrefs.edit(commit = true) {
            putBoolean(SETTINGS_WEATHER_SYNC_ENABLED, syncEnabled)
        }
        invalidateAndRefreshMode()
    }

    /**
     * [Flow] of calculation history entries.
     */
    fun historyFlow() = dataStore.historyFlow()

    /**
     * Schedules the removal of all calculation history entries.
     */
    fun deleteAllHistoryItems() = viewModelScope.launch { dataStore.deleteAll() }

    /**
     * Schedules the removal of this [historyItem] from the calculation history.
     */
    fun deleteHistoryItem(historyItem: HistoryItem) =
        viewModelScope.launch { dataStore.deleteHistoryItem(historyItem) }

    /**
     * Append a new [HistoryItem] to the calculation history.
     */
    fun onNewItem(duration: Duration) {
        viewModelScope.launch {
            dataStore.pushHistoryItem(
                HistoryItem(
                    duration,
                    calculateDistanceKm(duration),
                    _speedOfSound.value
                )
            )
        }
    }

    /**
     * Propagate a new user-declared speed of sound.
     */
    fun onUserSpeedOfSoundInput(sos: Double) {
        if (_speedMode.value != SpeedMode.USER) return
        _speedOfSound.value = sos
        _lastKnownUserSpeedOfSound.value = sos
        sharedPrefs.edit {
            putFloat(SETTINGS_LAST_USER_SPEED_OF_SOUND, sos.toFloat())
        }
    }

    /**
     * Convert [time] elapsed to distance in kilometres using the [speedOfSound].
     */
    fun calculateDistanceKm(time: Duration): Double {
        return (time.toDouble(DurationUnit.SECONDS) * _speedOfSound.value) / 1000
    }

    /**
     * Propagate an update to the user's location.
     */
    fun updateLocation(location: Location) {
        _userLocation.value = location
    }

    /**
     * Updates [speedOfSound] to the most accurate value using Weather APIs.
     */
    fun synchroniseSpeedOfSound() {
        // Ignore synchronisation if we're only accepting user inputs, waste of bandwidth
        invalidateAndRefreshMode()
        if (_speedMode.value == SpeedMode.USER)
            return
        Toast.makeText(application.applicationContext, "Syncing now...", Toast.LENGTH_SHORT)
            .show()
        viewModelScope.launch {
            val speed = withContext(Dispatchers.IO) {
                fetchSpeed()
            }
            if (_speedMode.value == SpeedMode.USER)
                return@launch
            _speedOfSound.value = speed
            _speedMode.value = SpeedMode.SYNCHRONISED
            Toast.makeText(application.applicationContext, "Sync complete!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private suspend fun fetchSpeed(): Double {
        // Busy wait until we have 1) a valid location or 2) no location availability by rejection
        while (_userLocation.value.provider == LOCATION_PENDING_PROVIDER && !locationUnavailable) {
            delay(100L)
        }
        if (locationUnavailable) {
            // Weather APIs will not function, we need to alert the user and swap to user mode
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    application.applicationContext,
                    "Syncing not possible without Location permission!",
                    Toast.LENGTH_LONG
                ).show()
            }
            _speedMode.value = SpeedMode.USER
            return 343.0
        }

        val tempC = 20 // TODO

        // v=\sqrt{\frac{\gamma RT}{M}}
        // where R is the molar gas constant (8.3145),
        // gamma is the adiabatic index (1.4),
        // M is the molar mass of dry air (0.0289645)
        // and T is ambient temperature in Kelvin
        return 331.3 * sqrt(1 + tempC / 273.15)
    }

    private fun invalidateAndRefreshMode() {
        _speedMode.value = if (sharedPrefs.getBoolean("weatherSyncEnabled", true))
            SpeedMode.FALLBACK
        else
            SpeedMode.USER
    }

    enum class SpeedMode {
        /**
         * Fetched by Weather API and calculated dynamically
         */
        SYNCHRONISED,

        /**
         * User input supplied override
         */
        USER,

        /**
         * Default when Weather APIs are still fetching
         */
        FALLBACK
    }

    companion object {
        /**
         * Whether an attempt to fetch location has failed and will not be expected.
         */
        var locationUnavailable = false
    }
}