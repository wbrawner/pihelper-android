package com.wbrawner.piholeclient

import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

const val NAME_BASE_URL = "baseUrl"

@Module
@InstallIn(SingletonComponent::class)
object PiHoleClientModule {
    @Provides
    @Singleton
    fun providesMoshi(): Moshi = Moshi.Builder().build()

    @Provides
    @Singleton
    fun providesOkHttpClient(): OkHttpClient {
        val client = OkHttpClient.Builder()
            .connectTimeout(500, TimeUnit.MILLISECONDS)
            .cookieJar(object : CookieJar {
                val cookies = mutableMapOf<String, List<Cookie>>()
                override fun loadForRequest(url: HttpUrl): List<Cookie> = cookies[url.host]
                    ?: emptyList()

                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    this.cookies[url.host] = cookies
                }
            })
        if (BuildConfig.DEBUG) {
            client.addInterceptor(HttpLoggingInterceptor(
                object : HttpLoggingInterceptor.Logger {
                    val moshi = Moshi.Builder()
                        .build()
                        .adapter(Any::class.java)
                        .indent("    ")

                    override fun log(message: String) {
                        val prettyMessage = try {
                            val json = JsonReader.of(Buffer().writeUtf8(message))
                            moshi.toJson(json.readJsonValue())
                        } catch (ignored: Exception) {
                            message
                        }
                        HttpLoggingInterceptor.Logger.DEFAULT.log(prettyMessage)
                    }
                })
                .apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
        }
        return client.build()
    }

    @Provides
    @Singleton
    fun providesPiHoleApiService(okHttpClient: OkHttpClient, moshi: Moshi): PiHoleApiService =
        OkHttpPiHoleApiService(okHttpClient, moshi)
}