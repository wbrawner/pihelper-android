package com.wbrawer.pihelper.shared

import com.wbrawner.pihelper.shared.KtorPiholeAPIService
import com.wbrawner.pihelper.shared.PiholeAPIService
import com.wbrawner.pihelper.shared.commonConfig
import io.ktor.client.*
import io.ktor.client.engine.android.*

fun PiholeAPIService.Companion.create() = KtorPiholeAPIService(HttpClient(Android) {
    commonConfig()
})