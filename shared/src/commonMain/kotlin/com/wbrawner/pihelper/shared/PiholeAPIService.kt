package com.wbrawner.pihelper.shared

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

const val BASE_PATH = "/admin/api.php"

interface PiholeAPIService {
    var baseUrl: String?
    var apiKey: String?
    suspend fun testConnection(): Boolean
    suspend fun getSummary(): Summary
    suspend fun getVersion(): VersionResponse
    suspend fun getTopItems(): TopItemsResponse
    suspend fun enable(): StatusResponse
    suspend fun disable(duration: Long? = null): StatusResponse
    suspend fun getDisabledDuration(): Long

    companion object
}

fun <T : HttpClientEngineConfig> HttpClientConfig<T>.commonConfig() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 1000
        connectTimeoutMillis = 1000
        socketTimeoutMillis = 1000
    }
}

class KtorPiholeAPIService(private val httpClient: HttpClient) : PiholeAPIService {
    private var port = 80
    override var baseUrl: String? = null
        set(value) {
            if (value?.contains(":") == true) {
                val parts = value.split(":")
                field = parts.first()
                port = parts.last().toInt()
            } else {
                field = value
            }
        }
    override var apiKey: String? = null

    override suspend fun getSummary(): Summary = httpClient.get {
        url {
            host = baseUrl ?: error("baseUrl not set")
            port = this@KtorPiholeAPIService.port
            encodedPath = BASE_PATH
            parameter("auth", apiKey)
            parameter("summary", "")
        }
    }.body()

    override suspend fun testConnection(): Boolean {
        val response = httpClient.head {
            url {
                host = baseUrl ?: error("baseUrl not set")
                port = this@KtorPiholeAPIService.port
                encodedPath = BASE_PATH
            }
        }
        return response.headers.contains("X-Pi-Hole")
    }

    override suspend fun getVersion(): VersionResponse = httpClient.get {
        url {
            host = baseUrl ?: error("baseUrl not set")
            port = this@KtorPiholeAPIService.port
            encodedPath = BASE_PATH
            parameter("version", "")
        }
    }.body()

    override suspend fun getTopItems(): TopItemsResponse = httpClient.get {
        url {
            host = baseUrl ?: error("baseUrl not set")
            port = this@KtorPiholeAPIService.port
            encodedPath = BASE_PATH
            parameter("topItems", "25")
            parameter("auth", apiKey)
        }
    }.body()

    override suspend fun enable(): StatusResponse = httpClient.get {
        url {
            host = baseUrl ?: error("baseUrl not set")
            port = this@KtorPiholeAPIService.port
            encodedPath = BASE_PATH
            parameter("enable", "")
            parameter("auth", apiKey)
        }
    }.body()

    override suspend fun disable(duration: Long?): StatusResponse = httpClient.get {
        url {
            host = baseUrl ?: error("baseUrl not set")
            port = this@KtorPiholeAPIService.port
            encodedPath = BASE_PATH
            parameter("disable", duration?.toString() ?: "")
            parameter("auth", apiKey)
        }
    }.body()

    override suspend fun getDisabledDuration(): Long = httpClient.get {
        url {
            host = baseUrl ?: error("baseUrl not set")
            port = this@KtorPiholeAPIService.port
            encodedPath = "/custom_disable_timer"
        }
    }.body<String>().toLong()
}
