package com.wbrawner.pihelper

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wbrawner.pihelper.shared.PiHoleApiService
import com.wbrawner.pihelper.shared.Status
import com.wbrawner.pihelper.shared.StatusProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

class PiHelperViewModel(
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