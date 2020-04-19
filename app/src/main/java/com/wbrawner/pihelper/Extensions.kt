package com.wbrawner.pihelper

import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.security.MessageDigest

fun String.hash(): String = BigInteger(
    1,
    MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
).toString(16).padStart(64, '0')

fun CoroutineScope.cancel() {
    coroutineContext[Job]?.cancel()
}

fun View.setSuspendingOnClickListener(
    coroutineScope: CoroutineScope,
    clickListener: suspend (v: View) -> Unit
) = setOnClickListener { v ->
    coroutineScope.launch { clickListener(v) }
}
