package com.wbrawner.pihelper

import java.math.BigInteger
import java.security.MessageDigest

fun String.hash(): String = BigInteger(
    1,
    MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
).toString(16)
