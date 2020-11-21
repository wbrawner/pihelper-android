package com.wbrawner.pihelper.shared

import io.ktor.client.*
import io.ktor.client.engine.ios.*

actual fun httpClient(): HttpClient = HttpClient(Ios)