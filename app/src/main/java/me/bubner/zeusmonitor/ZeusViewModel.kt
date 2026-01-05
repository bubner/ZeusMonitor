package me.bubner.zeusmonitor

import android.app.Application
import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.bubner.zeusmonitor.timer.HistoryDataStore
import me.bubner.zeusmonitor.timer.HistoryItem
import me.bubner.zeusmonitor.util.toLatLng
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.DurationUnit

private const val SETTINGS_WEATHER_SYNC_ENABLED = "weatherSyncEnabled"
private const val SETTINGS_LAST_USER_SPEED_OF_SOUND = "lastUserSpeedOfSound"
private const val LOCATION_PENDING_PROVIDER = "null"

class ZeusViewModel(app: Application) : AndroidViewModel(app) {
    private val client = HttpClient(Android)
    private var fetchSpeedJob: Job? = null
    private val sharedPrefs = app.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _lastKnownUserSpeedOfSound = MutableStateFlow(
        sharedPrefs.getFloat(SETTINGS_LAST_USER_SPEED_OF_SOUND, 343f).toDouble()
    )
    private val _speedOfSound = MutableStateFlow(_lastKnownUserSpeedOfSound.value) // m/s
    private val _speedMode = MutableStateFlow(SpeedMode.FALLBACK)
    private val _userLocation = MutableStateFlow(Location(LOCATION_PENDING_PROVIDER))
    private val _isFetchingWeather = MutableStateFlow(false)

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

    /**
     * Whether the current [fetchSpeedJob] is fetching weather from the API.
     */
    val isFetchingWeather: StateFlow<Boolean> = _isFetchingWeather

    /**
     * Whether location information is currently available. Will stay stuck if permission not granted.
     */
    // Recomposed whenever _userLocation is updated
    val isLocationAvailable
        get() = _userLocation.value.provider != LOCATION_PENDING_PROVIDER

    private val dataStore = HistoryDataStore(app.applicationContext)

    init {
        locationUnavailable = false
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
                    _speedOfSound.value,
                    _userLocation.value.toLatLng()
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
        fetchSpeedJob?.cancel()
        fetchSpeedJob = viewModelScope.launch {
            val speed = withContext(Dispatchers.IO) {
                fetchSpeed()
            }
            if (_speedMode.value == SpeedMode.USER || speed < 0)
                return@launch
            _speedOfSound.value = speed
            _speedMode.value = SpeedMode.SYNCHRONISED
            Toast.makeText(application.applicationContext, "Sync complete!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private suspend fun fetchSpeed(): Double {
        // Busy wait until we have 1) a valid location or 2) no location availability by rejection
        while (!isLocationAvailable && !locationUnavailable) {
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
            return -1.0
        }

        val latLng = _userLocation.value.toLatLng()
        try {
            _isFetchingWeather.value = true
            val weatherResponse = client.get("https://api.open-meteo.com/v1/forecast") {
                parameter("latitude", latLng.latitude)
                parameter("longitude", latLng.longitude)
                parameter("current", "temperature_2m")
                timeout {
                    connectTimeoutMillis = 5000
                    requestTimeoutMillis = 5000
                    socketTimeoutMillis = 5000
                }
            }

            // We opt to using static json parsing instead of serialising as we don't pass this data elsewhere
            val body = Json.parseToJsonElement(weatherResponse.bodyAsText()).jsonObject
            val tempC = body["current"]?.jsonObject["temperature_2m"]?.jsonPrimitive?.doubleOrNull

            if (weatherResponse.status.value == 400 || tempC == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        application.applicationContext,
                        "Failed to get weather data!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Log.e(
                    "ZeusMonitor",
                    "Failed to get weather data: ${body["reason"]?.jsonPrimitive?.content}"
                )
                _speedMode.value = SpeedMode.USER
                return -1.0
            }

            // v=\sqrt{\frac{\gamma RT}{M}}
            // where R is the molar gas constant (8.3145),
            // gamma is the adiabatic index (1.4),
            // M is the molar mass of dry air (0.0289645)
            // and T is ambient temperature in Kelvin
            return 331.3 * sqrt(1 + tempC / 273.15)
        } catch (e: ConnectTimeoutException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    application.applicationContext,
                    "Weather request timeout! Retrying...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            Log.e("ZeusMonitor", "Weather request timeout", e)
            // We try again which becomes a minimum rate of every 5 seconds
            return fetchSpeed()
        } finally {
            _isFetchingWeather.value = false
        }
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
         *
         * Note this variable's state does *not* cause recompositions.
         */
        // Initially true to allow @Preview annotations to function
        var locationUnavailable = true
    }
}