package com.wbrawner.pihelper

import android.util.Log
import androidx.lifecycle.ViewModel
import com.wbrawner.pihelper.shared.PiholeAPIService
import com.wbrawner.pihelper.shared.Status
import com.wbrawner.pihelper.shared.StatusProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

@HiltViewModel
class PiHelperViewModel @Inject constructor(
    private val apiService: PiholeAPIService
) : ViewModel() {
    private val _status = MutableStateFlow(Status.LOADING)
    val status = _status.asStateFlow()
    private var action: (suspend () -> StatusProvider)? = null
        get() = field ?: defaultAction
    private var defaultAction = suspend {
        apiService.getSummary()
    }

    suspend fun monitorSummary() {
        while (coroutineContext.isActive) {
            try {
                _status.value = action!!.invoke().status
                action = null
            } catch (ignored: Exception) {
                _status.value = Status.UNKNOWN
            }
            delay(1000)
        }
    }

    fun enablePiHole() {
        _status.value = Status.LOADING
        action = {
            apiService.enable()
        }
    }

    fun disablePiHole(duration: Long? = null) {
        _status.value = Status.LOADING
        action = {
            apiService.disable(duration)
        }
    }
}