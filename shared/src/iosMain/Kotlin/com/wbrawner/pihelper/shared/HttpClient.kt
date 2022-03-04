package com.wbrawner.pihelper.shared

import io.ktor.client.*
import io.ktor.client.engine.darwin.*

fun PiholeAPIService.Companion.create() = KtorPiholeAPIService(HttpClient(Darwin) {
    commonConfig()
})