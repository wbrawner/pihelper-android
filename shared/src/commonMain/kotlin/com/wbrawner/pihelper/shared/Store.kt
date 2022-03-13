package com.wbrawner.pihelper.shared

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class State(
    val apiKey: String? = null,
    val host: String? = null,
    val status: Status? = null,
    val scanning: String? = null,
    val loading: Boolean = false,
    val showAbout: Boolean = false,
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
                apiKey = apiKey
            )
            monitorChanges()
        } else {
            launch {
                connect("pi.hole")
            }
        }
    }

    fun dispatch(action: Action) {
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
            Action.About -> _state.value = _state.value.copy(showAbout = true)
            Action.Back -> back()
        }
    }

    private fun back() {
        when {
            _state.value.showAbout -> {
                _state.value = _state.value.copy(showAbout = false)
            }
            _state.value.status != null -> {
                launch {
                    _effects.emit(Effect.Exit)
                }
            }
            _state.value.scanning != null -> {
                _state.value = _state.value.copy(scanning = null)
                scanJob?.cancel("")
                scanJob = null
            }
            _state.value.apiKey != null -> {
                _state.value = _state.value.copy(apiKey = null)
            }
            _state.value.host != null -> {
                _state.value = _state.value.copy(host = null)
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
        scanJob = launch {
            val subnet = startingIp.substringBeforeLast(".")
            for (i in 0..255) {
                try {
                    val ip = "$subnet.$i"
                    _state.value = _state.value.copy(scanning = ip)
                    apiService.baseUrl = ip
                    apiService.getVersion()
                    _state.value = _state.value.copy(scanning = null)
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

    private fun connect(host: String) {
        _state.value = _state.value.copy(loading = true)
        launch {
            apiService.baseUrl = host
            try {
                apiService.getVersion()
                settings[KEY_HOST] = host
                _state.value = _state.value.copy(
                    host = host,
                    loading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false)
                _effects.emit(Effect.Error(e.message ?: "Failed to connect to $host"))
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
                    loading = false
                )
                monitorChanges()
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false)
                _effects.emit(Effect.Error(e.message ?: "Unable to authenticate with API key"))
            }
        }
    }

    private fun enable() {
        val loadingJob = launch {
            delay(500)
            _state.value = _state.value.copy(loading = true)
        }
        launch {
            try {
                apiService.enable()
                getStatus()
                loadingJob.cancel("")
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false)
                _effects.emit(Effect.Error(e.message ?: "Failed to enable Pi-hole"))
            }
        }
    }

    private fun disable(duration: Long?) {
        val loadingJob = launch {
            delay(500)
            _state.value = _state.value.copy(loading = true)
        }
        launch {
            try {
                apiService.disable(duration)
                getStatus()
                loadingJob.cancel("")
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false)
                _effects.emit(Effect.Error(e.message ?: "Failed to disable Pi-hole"))
            }
        }
    }

    private suspend fun getStatus() {
        // Don't set the state to loading here, otherwise it'll cause blinking animations
        try {
            val summary = apiService.getSummary()
            // TODO: If status is disabled, check for how long
            _state.value = _state.value.copy(status = summary.status, loading = false)
        } catch (e: Exception) {
            _state.value = _state.value.copy(loading = false)
            _effects.emit(Effect.Error(e.message ?: "Failed to load status"))
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
}

expect fun String.hash(): String