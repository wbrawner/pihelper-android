package com.wbrawner.pihelper.shared

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlin.math.floor

enum class Route(val path: String) {
    CONNECT("connect"),
    SCAN("scan"),
    AUTH("auth"),
    HOME("home"),
    ABOUT("about"),
}

data class State(
    val apiKey: String? = null,
    val host: String? = null,
    val status: Status? = null,
    val scanning: String? = null,
    val loading: Boolean = false,
    val route: Route = Route.CONNECT,
    val initialRoute: Route = Route.CONNECT
)

sealed interface AuthenticationString {
    val value: String

    data class Password(override val value: String) : AuthenticationString
    data class Token(override val value: String) : AuthenticationString
}

sealed interface Action {
    data class Connect(val host: String) : Action
    data class Scan(val deviceIp: String) : Action
    data class Authenticate(val authString: AuthenticationString) : Action
    object Enable : Action
    data class Disable(val duration: Long? = null) : Action
    object Forget : Action
    object About : Action
    object Back : Action
}

sealed interface Effect {
    object Exit : Effect
    data class Error(val message: String) : Effect
    object Empty
}

const val KEY_HOST = "baseUrl"
const val KEY_API_KEY = "apiKey"

private const val ONE_HOUR = 3_600_000
private const val ONE_MINUTE = 60_000
private const val ONE_SECOND = 1_000

class Store(
    private val apiService: PiholeAPIService,
    private val settings: Settings = Settings(),
    initialState: State = State()
) : CoroutineScope by CoroutineScope(Dispatchers.Main) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state
    private val _effects = MutableSharedFlow<Effect>()
    val effects: Flow<Effect> = _effects
    private var monitorJob: Job? = null
    private var scanJob: Job? = null

    init {
        launch {
            _state.collect {
                println(it)
            }
        }
        val host: String? = settings[KEY_HOST]
        val apiKey: String? = settings[KEY_API_KEY]
        if (!host.isNullOrBlank() && !apiKey.isNullOrBlank()) {
            apiService.baseUrl = host
            apiService.apiKey = apiKey
            _state.value = initialState.copy(
                host = host,
                apiKey = apiKey,
                route = Route.HOME,
                initialRoute = Route.HOME
            )
            monitorChanges()
        } else {
            launch {
                connect("pi.hole", false)
            }
        }
    }

    fun dispatch(action: Action) {
        println(action)
        when (action) {
            is Action.Authenticate -> {
                when (action.authString) {
                    // The Pi-hole API key is just the web password hashed twice with SHA-256
                    is AuthenticationString.Password -> authenticate(
                        action.authString.value.hash().hash()
                    )
                    is AuthenticationString.Token -> authenticate(action.authString.value)
                }
            }
            is Action.Connect -> connect(action.host)
            is Action.Disable -> disable(action.duration)
            Action.Enable -> enable()
            Action.Forget -> forget()
            is Action.Scan -> scan(action.deviceIp)
            Action.About -> _state.value = _state.value.copy(route = Route.ABOUT)
            Action.Back -> back()
        }
    }

    private fun back() {
        when (_state.value.route) {
            Route.ABOUT -> {
                _state.value = _state.value.copy(route = Route.HOME)
            }
            Route.AUTH -> {
                _state.value = _state.value.copy(apiKey = null, route = Route.CONNECT)
            }
            Route.HOME, Route.CONNECT -> {
                launch {
                    _effects.emit(Effect.Exit)
                }
            }
            Route.SCAN -> {
                scanJob?.cancel("")
                scanJob = null
                _state.value = _state.value.copy(scanning = null, route = Route.CONNECT)
            }
        }
    }

    private fun forget() {
        _state.value = State()
        settings.remove(KEY_HOST)
        settings.remove(KEY_API_KEY)
        monitorJob?.cancel("")
        monitorJob = null
    }

    private fun scan(startingIp: String) {
        _state.value = _state.value.copy(route = Route.SCAN)
        scanJob = launch {
            val subnet = startingIp.substringBeforeLast(".")
            repeat(256) { i ->
                try {
                    val ip = "$subnet.$i"
                    _state.value = _state.value.copy(scanning = ip)
                    apiService.baseUrl = ip
                    apiService.getVersion()
                    _state.value = _state.value.copy(scanning = null, host = ip, route = Route.AUTH)
                    scanJob = null
                    return@launch
                } catch (ignored: Exception) {
                }
            }
            _state.value = _state.value.copy(scanning = null)
            _effects.emit(Effect.Error("Failed to discover pi-hole on network"))
            scanJob = null
        }
    }

    private fun connect(host: String, emitError: Boolean = true) {
        _state.value = _state.value.copy(loading = true)
        launch {
            apiService.baseUrl = host
            try {
                apiService.getVersion()
                settings[KEY_HOST] = host
                _state.value = _state.value.copy(
                    host = host,
                    loading = false,
                    route = Route.AUTH,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false)
                if (emitError) {
                    _effects.emit(Effect.Error(e.message ?: "Failed to connect to $host"))
                }
            }
        }
    }

    private fun authenticate(token: String) {
        _state.value = _state.value.copy(loading = true)
        launch {
            apiService.apiKey = token
            try {
                apiService.getTopItems()
                settings[KEY_API_KEY] = token
                _state.value = _state.value.copy(
                    apiKey = token,
                    loading = false,
                    route = Route.HOME,
                )
                monitorChanges()
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false)
                _effects.emit(Effect.Error(e.message ?: "Unable to authenticate with API key"))
            }
        }
    }

    private fun enable() {
        launch {
            _state.value = _state.value.copy(loading = true)
            try {
                apiService.enable()
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false)
                _effects.emit(Effect.Error(e.message ?: "Failed to enable Pi-hole"))
            }
        }
    }

    private fun disable(duration: Long?) {
        launch {
            _state.value = _state.value.copy(loading = true)
            try {
                apiService.disable(duration)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false)
                _effects.emit(Effect.Error(e.message ?: "Failed to disable Pi-hole"))
            }
        }
    }

    private suspend fun getStatus() {
        val loadingJob = coroutineScope {
            launch {
                delay(1000)
                _state.value = _state.value.copy(loading = true)
            }
        }
        try {
            val summary = apiService.getSummary()
            var status = summary.status
            if (status is Status.Disabled) {
                try {
                    val until = apiService.getDisabledDuration()
                    val now = Clock.System.now().toEpochMilliseconds()
                    if (now > until) return
                    status = status.copy(timeRemaining = (until - now).toDurationString())
                } catch (ignored: Exception) {
                    // This isn't critical to the operation of the app so errors are unimportant
                    ignored.printStackTrace()
                }
            }
            loadingJob.cancel("")
            _state.value = _state.value.copy(status = status, loading = false)
        } catch (e: Exception) {
            e.printStackTrace()
            _state.value = _state.value.copy(loading = false)
            _effects.emit(Effect.Error(e.message ?: "Failed to load status"))
        } finally {
            loadingJob.cancel("")
        }
    }

    private fun monitorChanges() {
        monitorJob = launch {
            while (isActive) {
                getStatus()
                delay(1000)
            }
        }
    }

    companion object
}

expect fun String.hash(): String

fun Long.toDurationString(): String {
    var timeRemaining = toDouble()
    var formattedTimeRemaining = ""
    if (timeRemaining > ONE_HOUR) {
        formattedTimeRemaining += floor(timeRemaining / ONE_HOUR)
            .toInt()
            .toString() + ":"
        timeRemaining %= ONE_HOUR
    }
    if (timeRemaining > ONE_MINUTE) {
        val minutesLength = if (formattedTimeRemaining.isBlank()) 1 else 2
        formattedTimeRemaining += floor(timeRemaining / ONE_MINUTE)
            .toInt()
            .toString()
            .padStart(minutesLength, '0') + ':'
        timeRemaining %= ONE_MINUTE
    } else if (formattedTimeRemaining.isNotBlank()) {
        formattedTimeRemaining += "00:"
    }
    val secondsLength = if (formattedTimeRemaining.isBlank()) 1 else 2
    formattedTimeRemaining += floor(timeRemaining / ONE_SECOND)
        .toInt()
        .toString()
        .padStart(secondsLength, '0')
    return formattedTimeRemaining
}