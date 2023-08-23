package com.wbrawner.pihelper.shared

import java.math.BigInteger
import java.security.MessageDigest


actual fun String.hash(): String = BigInteger(
    1,
    MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
).toString(16).padStart(64, '0')
