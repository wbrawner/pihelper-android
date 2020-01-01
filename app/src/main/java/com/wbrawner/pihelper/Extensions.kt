package com.wbrawner.pihelper

fun <T, R> R.transform(block: (R) -> T): T = block(this)