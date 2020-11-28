package com.wbrawner.pihelper.shared

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.*
import kotlinx.serialization.json.Json

actual fun httpClient(): HttpClient = HttpClient(Android) {
    install(JsonFeature) {
        serializer = KotlinxSerializer(Json {
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}