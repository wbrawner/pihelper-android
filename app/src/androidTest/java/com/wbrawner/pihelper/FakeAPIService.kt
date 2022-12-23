package com.wbrawner.pihelper

import com.wbrawner.pihelper.shared.PiholeAPIService
import com.wbrawner.pihelper.shared.create
import okhttp3.mockwebserver.MockWebServer

class FakeAPIService(
    private val apiService: PiholeAPIService = PiholeAPIService.create()
) : PiholeAPIService by apiService {
    val server = MockWebServer().apply {
        start()
    }

    val hostName: String = server.hostName
    val port = server.port
}