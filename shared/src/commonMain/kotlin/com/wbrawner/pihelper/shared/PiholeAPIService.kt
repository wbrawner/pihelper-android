package com.wbrawner.pihelper.shared

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

const val BASE_PATH = "/admin/api.php"

abstract class PiholeAPIService {
    abstract var baseUrl: String?
    abstract var apiKey: String?

    abstract suspend fun getSummary(): Summary

    abstract suspend fun getVersion(): VersionResponse
    abstract suspend fun getTopItems(): TopItemsResponse
    abstract suspend fun enable(): StatusResponse
    abstract suspend fun disable(duration: Long? = null): StatusResponse

    companion object
}

fun <T: HttpClientEngineConfig> HttpClientConfig<T>.commonConfig() {
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

class KtorPiholeAPIService(val httpClient: HttpClient) : PiholeAPIService() {
    override var baseUrl: String? = null
    override var apiKey: String? = null
        get() {
            println("apiKey: $field")
            return field
        }

    override suspend fun getSummary(): Summary = httpClient.get {
        url {
            host = baseUrl ?: error("baseUrl not set")
            encodedPath = BASE_PATH
        }
    }.body()

    override suspend fun getVersion(): VersionResponse = httpClient.get {
        url {
            host = baseUrl ?: error("baseUrl not set")
            encodedPath = BASE_PATH
            parameter("version", "")
        }
    }.body()

    override suspend fun getTopItems(): TopItemsResponse = httpClient.get {
        url {
            host = baseUrl ?: error("baseUrl not set")
            encodedPath = BASE_PATH
            parameter("topItems", "25")
            parameter("auth", apiKey)
        }
    }.body()

    override suspend fun enable(): StatusResponse = httpClient.get {
        url {
            host = baseUrl ?: error("baseUrl not set")
            encodedPath = BASE_PATH
            parameter("enable", "")
            parameter("auth", apiKey)
        }
    }.body()

    override suspend fun disable(duration: Long?): StatusResponse = httpClient.get {
        url {
            host = baseUrl ?: error("baseUrl not set")
            encodedPath = BASE_PATH
            parameter("disable", duration?.toString()?: "")
            parameter("auth", apiKey)
        }
    }.body()
}
