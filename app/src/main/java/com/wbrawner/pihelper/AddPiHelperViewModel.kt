package com.wbrawner.pihelper

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.wbrawner.pihelper.shared.PiholeAPIService
import com.wbrawner.pihelper.shared.VersionResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject

const val KEY_BASE_URL = "baseUrl"
const val KEY_API_KEY = "apiKey"
const val IP_MIN = 0
const val IP_MAX = 255

@HiltViewModel
class AddPiHelperViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val apiService: PiholeAPIService
) : ViewModel() {

    @Volatile
    var baseUrl: String? = sharedPreferences.getString(KEY_BASE_URL, null)
        set(value) {
            sharedPreferences.edit {
                putString(KEY_BASE_URL, value)
            }
            field = value
        }

    @Volatile
    var apiKey: String? = sharedPreferences.getString(KEY_API_KEY, null)
        set(value) {
            sharedPreferences.edit {
                putString(KEY_API_KEY, value)
            }
            field = value
        }

    init {
        apiService.baseUrl = this.baseUrl
        apiService.apiKey = this.apiKey
    }

    val loadingMessage = MutableStateFlow<String?>(null)
    val errorMessage = MutableStateFlow<String?>(null)

    suspend fun beginScanning(deviceIpAddress: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val addressParts = deviceIpAddress.split(".").toMutableList()
        var chunks = 1
        // If the Pi-hole is correctly set up, then there should be a special host for it as
        // "pi.hole"
        val ipAddresses = mutableListOf("pi.hole")
        while (chunks <= IP_MAX) {
            val chunkSize = (IP_MAX - IP_MIN + 1) / chunks
            if (chunkSize == 1) {
                return
            }
            for (chunk in 0 until chunks) {
                val chunkStart = IP_MIN + (chunk * chunkSize)
                val chunkEnd = IP_MIN + ((chunk + 1) * chunkSize)
                addressParts[3] = (((chunkEnd - chunkStart) / 2) + chunkStart).toString()
                ipAddresses.add(addressParts.joinToString("."))
            }
            chunks *= 2
        }
        if (scan(ipAddresses)) {
            onSuccess()
        } else {
            onFailure()
        }
    }

    private suspend fun scan(ipAddresses: MutableList<String>): Boolean {
        if (ipAddresses.isEmpty()) {
            loadingMessage.value = null
            return false
        }

        val ipAddress = ipAddresses.removeAt(0)
        loadingMessage.value = "Scanning $ipAddress..."
        return if (!connectToIpAddress(ipAddress)) {
            scan(ipAddresses)
        } else {
            true
        }
    }

    suspend fun connectToIpAddress(ipAddress: String): Boolean {
        val version: VersionResponse? = withContext(Dispatchers.IO) {
            try {
                apiService.baseUrl = ipAddress
                apiService.getVersion()
            } catch (ignored: ConnectException) {
                null
            } catch (ignored: SocketTimeoutException) {
                null
            } catch (e: Exception) {
                Log.e("Pi-helper", "Failed to load Pi-Hole version at $ipAddress", e)
                null
            }
        }
        if (version != null) {
            baseUrl = ipAddress
            return true
        }
        return false
    }

    suspend fun authenticateWithPassword(password: String): Boolean {
        // The Pi-hole API key is just the web password hashed twice with SHA-256
        return authenticateWithApiKey(password.hash().hash())
    }

    suspend fun authenticateWithApiKey(apiKey: String): Boolean {
        // This uses the topItems endpoint to test that the API key is working since it requires
        // authentication and is fairly simple to determine whether or not the request was
        // successful
        errorMessage.value = null
        apiService.apiKey = apiKey
        return try {
            apiService.getTopItems()
            this.apiKey = apiKey
            true
        } catch (e: Exception) {
            Log.e("Pi-helper", "Unable to authenticate with API key", e)
            errorMessage.value = "Authentication failed"
            false
        }
    }

    fun forgetPihole() {
        baseUrl = null
        apiKey = null
    }
}
