package com.wbrawner.pihelper

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wbrawner.piholeclient.PiHoleApiService
import com.wbrawner.piholeclient.Summary
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import java.lang.Exception
import kotlin.coroutines.coroutineContext

class PiHelperViewModel(
    private val apiService: PiHoleApiService
) : ViewModel() {
    val summary = MutableLiveData<Summary>()

    suspend fun monitorSummary() {
        while (coroutineContext.isActive) {
            try {
                loadSummary()
            } catch (ignored: Exception) {
                break
            }
            delay(1000)
        }
    }

    suspend fun loadSummary() {
        summary.postValue(apiService.getSummary())
    }

    suspend fun enablePiHole() {
        apiService.enable()
    }

    suspend fun disablePiHole(duration: Long? = null) {
        apiService.disable(duration)
    }
}