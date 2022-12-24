package com.wbrawner.pihelper.util

import android.content.Context
import com.wbrawner.pihelper.shared.PiholeAPIService
import com.wbrawner.pihelper.shared.create
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class FakeAPIService : PiholeAPIService by PiholeAPIService.create() {
    val server = MockWebServer().apply {
        start()
    }
    val hostName: String = server.hostName
    val port = server.port

    fun testConnectionSuccess() {
        server.enqueue(
            MockResponse().setHeader(
                "X-Pi-Hole",
                "The Pi-hole Web interface is working!"
            )
        )
    }

    fun testConnectionFailure() {
        server.enqueue(MockResponse().setResponseCode(204))
    }

    fun authenticationSuccess(context: Context) {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(
                    context.readAsset("json/top_items_success.json")
                )
        )
    }

    fun authenticationFailure(context: Context) {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(
                    context.readAsset("json/top_items_failure.json")
                )
        )
    }

    fun statusEnabled(context: Context) {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(
                    context.readAsset("json/summary_enabled.json")
                )
        )
    }

    fun statusDisabled(context: Context, duration: Long? = null) {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(
                    context.readAsset("json/summary_disabled.json")
                )
        )
    }

    fun enableSuccess(context: Context) {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(
                    context.readAsset("json/status_enabled.json")
                )
        )
    }

    fun disableSuccess(context: Context) {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(
                    context.readAsset("json/status_disabled.json")
                )
        )
    }

    fun disabledPermanently() {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "text/html")
                .setBody("<p>Definitely not a number</p>")
        )
    }
}

private fun Context.readAsset(path: String) = assets.open(path)
    .bufferedReader()
    .use { it.readText() }