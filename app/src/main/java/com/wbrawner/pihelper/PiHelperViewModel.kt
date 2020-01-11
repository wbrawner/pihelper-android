package com.wbrawner.pihelper

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wbrawner.piholeclient.PiHoleApiService
import com.wbrawner.piholeclient.Status
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.lang.Exception
import kotlin.coroutines.coroutineContext

class PiHelperViewModel(
    private val apiService: PiHoleApiService
) : ViewModel() {
    val status = MutableLiveData<Status>()
    private var action: (suspend () -> Status)? = null
        get() = field ?: defaultAction
    private var defaultAction = suspend {
        apiService.getStatus()
    }

    suspend fun monitorSummary() {
        while (coroutineContext.isActive) {
            try {
                status.postValue(action!!.invoke())
                action = null
            } catch (ignored: Exception) {
                break
            }
            delay(1000)
        }
    }

    suspend fun enablePiHole() {
        action = {
            apiService.enable()
        }
    }

    suspend fun disablePiHole(duration: Long? = null) {
        action = {
            apiService.disable(duration)
        }
    }
}