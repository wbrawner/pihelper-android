package com.wbrawner.pihelper.shared

import io.ktor.client.*
import io.ktor.client.engine.android.*

fun PiholeAPIService.Companion.create() = KtorPiholeAPIService(HttpClient(Android) {
    commonConfig()
})