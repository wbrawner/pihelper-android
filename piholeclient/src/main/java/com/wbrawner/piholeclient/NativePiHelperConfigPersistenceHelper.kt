package com.wbrawner.piholeclient

import com.wbrawner.libpihelper.PiHelperNative
import java.io.File

class NativePiHelperConfigPersistenceHelper(private val configFile: File): ConfigPersistenceHelper {
    init {
        if (configFile.exists()) {
            PiHelperNative.readConfig(configFile.absolutePath)
        } else {
            PiHelperNative.initConfig()
        }
    }

    override suspend fun getHost(): String? = PiHelperNative.getHost()

    override suspend fun setHost(host: String?) {
        PiHelperNative.setHost(host)
    }

    override suspend fun getApiKey(): String? = PiHelperNative.getApiKey()

    override suspend fun setApiKey(apiKey: String?) {
        PiHelperNative.setApiKey(apiKey)
    }

    override suspend fun setPassword(password: String?) {
        PiHelperNative.setPassword(password)
    }

    override suspend fun saveConfig() {
        PiHelperNative.saveConfig(configFile.absolutePath)
    }

    override suspend fun deleteConfig() {
        PiHelperNative.cleanup()
        configFile.delete()
        PiHelperNative.initConfig()
    }
}