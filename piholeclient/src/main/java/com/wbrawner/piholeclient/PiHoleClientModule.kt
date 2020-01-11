package com.wbrawner.piholeclient

import android.app.Application
import org.koin.dsl.module
import java.io.File

const val DEFAULT_CONFIG_FILE = "pihelper.conf"

val piHoleClientModule = module {

    single<PiHoleApiService> {
        NativePiHoleApiService()
    }

    single<ConfigPersistenceHelper> {
        NativePiHelperConfigPersistenceHelper(get())
    }

    single {
        File(get<Application>().filesDir, DEFAULT_CONFIG_FILE)
    }
}