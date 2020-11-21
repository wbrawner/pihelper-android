package com.wbrawner.pihelper.shared

import io.ktor.client.*
import io.ktor.client.engine.android.*

actual fun httpClient(): HttpClient = HttpClient(Android)