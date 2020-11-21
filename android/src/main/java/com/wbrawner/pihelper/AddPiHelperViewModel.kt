package com.wbrawner.pihelper

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wbrawner.pihelper.shared.KtorPiHoleApiService
import com.wbrawner.pihelper.shared.PiHoleApiService
import com.wbrawner.pihelper.shared.VersionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException

const val KEY_BASE_URL = "baseUrl"
const val KEY_API_KEY = "apiKey"
const val IP_MIN = 0
const val IP_MAX = 255

class AddPiHelperViewModel(
    private val sharedPreferences: SharedPreferences,
    private val apiService: PiHoleApiService
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

    val piHoleIpAddress = MutableLiveData<String?>()
    val scanningIp = MutableLiveData<String?>()
    val authenticated = MutableLiveData<Boolean>()

    suspend fun beginScanning(deviceIpAddress: String) {
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
        scan(ipAddresses)
    }

    private suspend fun scan(ipAddresses: MutableList<String>) {
        if (ipAddresses.isEmpty()) {
            scanningIp.postValue(null)
            piHoleIpAddress.postValue(null)
            return
        }

        val ipAddress = ipAddresses.removeAt(0)
        scanningIp.postValue(ipAddress)
        if (!connectToIpAddress(ipAddress)) {
            scan(ipAddresses)
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
        return if (version == null) {
            false
        } else {
            piHoleIpAddress.postValue(ipAddress)
            baseUrl = ipAddress
            true
        }
    }

    suspend fun authenticateWithPassword(password: String) {
        // The Pi-hole API key is just the web password hashed twice with SHA-256
        authenticateWithApiKey(password.hash().hash())
    }

    suspend fun authenticateWithApiKey(apiKey: String) {
        // This uses the topItems endpoint to test that the API key is working since it requires
        // authentication and is fairly simple to determine whether or not the request was
        // successful
        apiService.apiKey = apiKey
        try {
            apiService.getTopItems()
            this.apiKey = apiKey
            authenticated.postValue(true)
        } catch (e: Exception) {
            Log.e("Pi-helper", "Unable to authenticate with API key", e)
            authenticated.postValue(false)
            throw e
        }
    }

    fun forgetPihole() {
        baseUrl = null
        apiKey = null
    }
}
