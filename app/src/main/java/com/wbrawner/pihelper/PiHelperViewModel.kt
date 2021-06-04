package com.wbrawner.pihelper

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wbrawner.piholeclient.PiHoleApiService
import com.wbrawner.piholeclient.Status
import com.wbrawner.piholeclient.StatusProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

@HiltViewModel
class PiHelperViewModel @Inject constructor(
    private val apiService: PiHoleApiService
) : ViewModel() {
    val status = MutableLiveData<Status>()
    private var action: (suspend () -> StatusProvider)? = null
        get() = field ?: defaultAction
    private var defaultAction = suspend {
        apiService.getSummary()
    }

    suspend fun monitorSummary() {
        while (coroutineContext.isActive) {
            try {
                status.postValue(action!!.invoke().status)
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