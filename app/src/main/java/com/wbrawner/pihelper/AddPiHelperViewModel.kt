package com.wbrawner.pihelper

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wbrawner.piholeclient.ConfigPersistenceHelper
import com.wbrawner.piholeclient.PiHoleApiService
import com.wbrawner.piholeclient.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException

const val IP_MIN = 0
const val IP_MAX = 255

class AddPiHelperViewModel(
    private val apiService: PiHoleApiService,
    private val configHelper: ConfigPersistenceHelper
) : ViewModel() {

    val baseUrl: String? = configHelper.host
    val apiKey: String? = configHelper.apiKey

    val piHoleIpAddress = MutableLiveData<String?>()
    val scanningIp = MutableLiveData<String?>()
    val authenticated = MutableLiveData<Boolean>()

    suspend fun beginScanning(deviceIpAddress: String) {
        val addressParts = deviceIpAddress.split(".").toMutableList()
        var chunks = 1
        // TODO: Move this to the native lib
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
        val status: Status? = withContext(Dispatchers.IO) {
            try {
                configHelper.host = ipAddress
                apiService.getStatus()
            } catch (ignored: ConnectException) {
                null
            } catch (ignored: SocketTimeoutException) {
                null
            } catch (e: Exception) {
                Log.e("Pi-helper", "Failed to load Pi-Hole version at $ipAddress", e)
                null
            }
        }
        return if (status == null || status == Status.FAILURE) {
            false
        } else {
            piHoleIpAddress.postValue(ipAddress)
            configHelper.saveConfig()
            true
        }
    }

    fun authenticateWithPassword(password: String) {
        configHelper.setPassword(password)
        configHelper.saveConfig()
        authenticate()
    }

    fun authenticateWithApiKey(apiKey: String) {
        configHelper.apiKey = apiKey
        configHelper.saveConfig()
        authenticate()
    }

    private fun authenticate() {
        try {
            // This uses the topItems endpoint to test that the API key is working since it requires
            // authentication and is fairly simple to determine whether or not the request was
            // successful
//            apiService.getTopItems()
            authenticated.postValue(true)
        } catch (e: Exception) {
            Log.e("Pi-Helper", "Unable to authenticate with API key", e)
            authenticated.postValue(false)
            throw e
        }
    }

    fun forgetPihole() {
        configHelper.deleteConfig()
    }
}
