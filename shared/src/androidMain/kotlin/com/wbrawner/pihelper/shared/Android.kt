package com.wbrawner.pihelper.shared

import io.ktor.client.*
import io.ktor.client.engine.android.*
import java.math.BigInteger
import java.security.MessageDigest

fun PiholeAPIService.Companion.create() = KtorPiholeAPIService(HttpClient(Android) {
    commonConfig()
})

actual fun String.hash(): String = BigInteger(
    1,
    MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
).toString(16).padStart(64, '0')
