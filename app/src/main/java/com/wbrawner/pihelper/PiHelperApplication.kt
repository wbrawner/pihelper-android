package com.wbrawner.pihelper

import android.app.Application
import android.content.Context
import com.wbrawner.piholeclient.piHoleClientModule
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraMailSender
import org.acra.annotation.AcraNotification
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

@Suppress("unused")
@AcraCore(buildConfigClass = BuildConfig::class)
@AcraMailSender(mailTo = "pihelper@wbrawner.com")
@AcraNotification(
    resIcon = R.drawable.ic_notification,
    resTitle = R.string.title_crash_notification,
    resText = R.string.text_crash_notification,
    resChannelName = R.string.channel_crash_notification
)
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

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        ACRA.init(this)
    }
}