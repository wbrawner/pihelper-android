package com.wbrawner.piholeclient

import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.reflect.KClass

interface PiHoleApiService {
    var baseUrl: String?
    var apiKey: String?

    suspend fun login(password: String): String

    suspend fun getApiToken(): String

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
const val INDEX_PATH = "/admin/index.php"
const val API_TOKEN_PATH = "/admin/scripts/pi-hole/php/api_token.php"

class OkHttpPiHoleApiService(
    private val okHttpClient: OkHttpClient,
    private val moshi: Moshi
) : PiHoleApiService {
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

    private val urlBuilder: HttpUrl.Builder
        get() {
            val host = baseUrl ?: throw IllegalStateException("No base URL defined")
            return HttpUrl.Builder()
                .scheme("http")
                .host(host)
                .addPathSegments(BASE_PATH)
        }

    override suspend fun getSummary(version: Boolean, type: Boolean): Summary {
        val url = urlBuilder
            .addQueryParameter("summary", "")
        if (version) {
            url.addQueryParameter("version", "")
        }
        if (type) {
            url.addQueryParameter("type", "")
        }
        val request = Request.Builder()
            .get()
            .url(url.build())
        return sendRequest(request.build(), Summary::class)!!
    }

    override suspend fun getVersion(): VersionResponse {
        val url = urlBuilder
            .addQueryParameter("version", "")
        val request = Request.Builder()
            .get()
            .url(url.build())
        return sendRequest(request.build(), VersionResponse::class)!!
    }

    override suspend fun getTopItems(): TopItemsResponse {
        val apiToken = this.apiKey ?: throw java.lang.IllegalStateException("No API Token provided")
        val url = urlBuilder
            .addQueryParameter("topItems", "25")
            .addQueryParameter("auth", apiToken)
        val request = Request.Builder()
            .get()
            .url(url.build())
        return sendRequest(request.build(), TopItemsResponse::class)!!
    }

    override suspend fun login(password: String): String {
        val url = urlBuilder
            .encodedPath(INDEX_PATH)
            .addQueryParameter("login", "")
        val body = "pw=$password".toRequestBody("application/x-www-form-urlencoded".toMediaType())
        val request = Request.Builder()
            .post(body)
            .url(url.build())
        return sendRequest(request.build(), String::class)!!
    }

    override suspend fun getApiToken(): String {
        val url = urlBuilder
            .encodedPath(API_TOKEN_PATH)
        val request = Request.Builder()
            .get()
            .url(url.build())
        return sendRequest(request.build(), String::class)!!
    }

    override suspend fun enable(): StatusResponse {
        val apiToken = this.apiKey ?: throw java.lang.IllegalStateException("No API Token provided")
        val url = urlBuilder
            .addQueryParameter("enable", "")
            .addQueryParameter("auth", apiToken)
        val request = Request.Builder()
            .get()
            .url(url.build())
        return sendRequest(request.build(), StatusResponse::class)!!
    }

    override suspend fun disable(duration: Long?): StatusResponse {
        val apiToken = this.apiKey ?: throw java.lang.IllegalStateException("No API Token provided")
        val url = urlBuilder
            .addQueryParameter("disable", duration?.toString()?: "")
            .addQueryParameter("auth", apiToken)
        val request = Request.Builder()
            .get()
            .url(url.build())
        return sendRequest(request.build(), StatusResponse::class)!!
    }

    private suspend fun <T : Any> sendRequest(request: Request, responseType: KClass<T>?): T? {
        return withContext(Dispatchers.IO) {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                null
            } else {
                @Suppress("UNCHECKED_CAST")
                when (responseType) {
                    null -> null
                    String::class -> response.body?.string() as T
                    else -> response.body?.let {
                        moshi
                            .adapter(responseType.javaObjectType)
                            .fromJson(it.source())
                    }
                }
            }
        }
    }
}