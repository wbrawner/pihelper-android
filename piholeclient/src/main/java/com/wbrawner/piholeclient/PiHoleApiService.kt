package com.wbrawner.piholeclient

import com.wbrawner.libpihelper.PiHelperNative
import java.io.File

interface PiHoleApiService {

    suspend fun getStatus(): Status

//    suspend fun getTopItems(): TopItems
    suspend fun enable(): Status
    suspend fun disable(duration: Long? = null): Status

}

class NativePiHoleApiService : PiHoleApiService {

//    override suspend fun getTopItems(): TopItems {
//        return PiHelperNative.getStatus
//    }

    override suspend fun getStatus(): Status {
        return Status.values()[PiHelperNative.getStatus() + 1]
    }

    override suspend fun enable(): Status {
        return Status.values()[PiHelperNative.enable() + 1]
    }

    override suspend fun disable(duration: Long?): Status {
        return Status.values()[PiHelperNative.disable(duration) + 1]
    }
}