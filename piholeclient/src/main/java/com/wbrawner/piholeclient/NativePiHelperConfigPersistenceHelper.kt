package com.wbrawner.piholeclient

import com.wbrawner.libpihelper.PiHelperNative
import java.io.File

class NativePiHelperConfigPersistenceHelper(private val configFile: File) :
    ConfigPersistenceHelper {
    init {
        if (configFile.exists()) {
            PiHelperNative.readConfig(configFile.absolutePath)
        } else {
            PiHelperNative.initConfig()
        }
    }

    override var host: String?
        get() = PiHelperNative.getHost()
        set(value) {
            PiHelperNative.setHost(value)
        }

    override var apiKey: String?
        get() = PiHelperNative.getApiKey()
        set(value) {
            PiHelperNative.setApiKey(value)
        }

    override fun setPassword(password: String?) {
        PiHelperNative.setPassword(password)
    }

    override fun saveConfig() {
        PiHelperNative.saveConfig(configFile.absolutePath)
    }

    override fun deleteConfig() {
        PiHelperNative.cleanup()
        configFile.delete()
        PiHelperNative.initConfig()
    }
}