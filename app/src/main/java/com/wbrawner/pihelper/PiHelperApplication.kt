package com.wbrawner.pihelper

import android.app.Application
import android.content.Context
import com.wbrawner.piholeclient.piHoleClientModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

@Suppress("unused")
class PiHelperApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidLogger()
            androidContext(this@PiHelperApplication)
            modules(listOf(
                piHoleClientModule,
                piHelperModule
            ))
        }
    }
}