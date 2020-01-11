package com.wbrawner.piholeclient

interface ConfigPersistenceHelper {
    suspend fun getHost(): String?
    suspend fun setHost(host: String?)
    suspend fun getApiKey(): String?
    suspend fun setApiKey(apiKey: String?)
    suspend fun setPassword(password: String?)
    suspend fun saveConfig()
    suspend fun deleteConfig()
}

