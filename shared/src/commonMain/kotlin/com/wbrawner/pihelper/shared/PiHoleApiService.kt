package com.wbrawner.pihelper.shared

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.jvm.Synchronized
import kotlin.reflect.KClass

interface PiHoleApiService {
    var baseUrl: String?
    var apiKey: String?

    suspend fun getSummary(
        version: Boolean = false,
        type: Boolean = false
    ): Summary

    suspend fun getVersion(): VersionResponse

    suspend fun getTopItems(): TopItemsResponse

    suspend fun enable(): StatusResponse

    suspend fun disable(duration: Long? = null): StatusResponse

    /**
    @Query("overTimeData10mins") overTimeData10mins: Boolean = true,
    @Query("topItems") topItems: Int? = null,
    @Query("topClients") topClients: Int? = null,
    @Query("getForwardDestinations") getForwardDestinations: Boolean = true,
    @Query("getQueryTypes") getQueryTypes: Boolean = true,
    @Query("getAllQueries") getAllQueries: Boolean = true

     */

//    suspend fun login(password: String): Response<String>
//
//    @GET("/admin/scripts/pi-hole/php/api_token.php")
//    suspend fun apiKey(phpSession: String): Response<String>
}

const val BASE_PATH = "/admin/api.php"

expect fun httpClient(): HttpClient

class KtorPiHoleApiService(private val httpClient: HttpClient = httpClient()) : PiHoleApiService {
    override var baseUrl: String? = null
        @Synchronized
        get
        @Synchronized
        set
    override var apiKey: String? = null
        @Synchronized
        get
        @Synchronized
        set

    private fun urlBuilder(configuration: URLBuilder.() -> Unit): Url {
        val host = baseUrl ?: throw IllegalStateException("No base URL defined")
        return URLBuilder(
            protocol = URLProtocol.HTTP,
            host = host,
            encodedPath = BASE_PATH
        ).run {
            configuration()
            build()
        }
    }

    override suspend fun getSummary(version: Boolean, type: Boolean): Summary = httpClient.get {
        url(urlBuilder {
            parameters["summary"] = ""
            if (type) {
                parameters["type"] = ""
            }
        })
    }

    override suspend fun getVersion(): VersionResponse = httpClient.get {
        url(urlBuilder {
            parameters["version"] = ""
        })
    }

    override suspend fun getTopItems(): TopItemsResponse = httpClient.get {
        val apiToken = apiKey ?: throw Error("No API Token provided")
        url(urlBuilder {
            parameters["topItems"] = "25"
            parameters["auth"] = apiToken
        })
    }

    override suspend fun enable(): StatusResponse = httpClient.get {
        val apiToken = apiKey ?: throw Error("No API Token provided")
        url(urlBuilder {
            parameters["enable"] = ""
            parameters["auth"] = apiToken
        })
    }

    override suspend fun disable(duration: Long?): StatusResponse = httpClient.get {
        val apiToken = apiKey ?: throw Error("No API Token provided")
        url(urlBuilder {
            parameters["disable"] = duration?.toString() ?: ""
            parameters["auth"] = apiToken
        })
    }
}