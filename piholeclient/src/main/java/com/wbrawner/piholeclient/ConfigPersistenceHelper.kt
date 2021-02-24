package com.wbrawner.piholeclient

interface ConfigPersistenceHelper {
    var host: String?
    var apiKey: String?
    fun setPassword(password: String?)
    fun saveConfig()
    fun deleteConfig()
}

