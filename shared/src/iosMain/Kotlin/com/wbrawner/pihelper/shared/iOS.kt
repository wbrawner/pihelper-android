package com.wbrawner.pihelper.shared

import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH

fun PiholeAPIService.Companion.create() = KtorPiholeAPIService(HttpClient(Darwin) {
    commonConfig()
})

fun Store.Companion.create() = Store(PiholeAPIService.create())

// TODO: This doesn't actually produce the correct values
actual fun String.hash(): String = toByteArray(Charsets.UTF_8).run {
    val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
    usePinned { inputPinned ->
        digest.usePinned { digestPinned ->
            CC_SHA256(inputPinned.addressOf(0), this.size.convert(), digestPinned.addressOf(0))
        }
    }

    return digest.joinToString(separator = "", transform = { it.toString(16) })
        .also {
            println("hash for ${this@hash} == $it")
        }
}

fun Store.watchState(block: (State) -> Unit) = state.watch(block)
fun Store.watchEffects(block: (Effect) -> Unit) = effects.watch(block)

fun interface Closeable {
    fun close()
}

class CloseableFlow<T : Any> internal constructor(private val origin: Flow<T>) : Flow<T> by origin {
    fun watch(block: (T) -> Unit): Closeable {
        val job = Job()

        onEach {
            block(it)
        }.launchIn(CoroutineScope(Dispatchers.Main + job))

        return Closeable { job.cancel() }
    }
}

internal fun <T : Any> Flow<T>.watch(block: (T) -> Unit): Closeable =
    CloseableFlow(this).watch(block)