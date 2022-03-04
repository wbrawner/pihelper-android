package com.wbrawer.pihelper.shared

import com.wbrawner.pihelper.shared.KtorPiholeAPIService
import com.wbrawner.pihelper.shared.PiholeAPIService
import io.ktor.client.*
import com.wbrawner.pihelper.shared.commonConfig
import io.ktor.client.engine.darwin.*

fun PiholeAPIService.Companion.create() = KtorPiholeAPIService(HttpClient(Darwin) {
    commonConfig()
})