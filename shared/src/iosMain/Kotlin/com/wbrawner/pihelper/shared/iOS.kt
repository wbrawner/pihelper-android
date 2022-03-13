package com.wbrawner.pihelper.shared

import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH

fun PiholeAPIService.Companion.create() = KtorPiholeAPIService(HttpClient(Darwin) {
    commonConfig()
})

actual fun String.hash(): String {
    val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
    usePinned { inputPinned ->
        digest.usePinned { digestPinned ->
            CC_SHA256(inputPinned.addressOf(0), this.length.convert(), digestPinned.addressOf(0))
        }
    }
    return digest.joinToString(separator = "") { it.toString(16) }
}