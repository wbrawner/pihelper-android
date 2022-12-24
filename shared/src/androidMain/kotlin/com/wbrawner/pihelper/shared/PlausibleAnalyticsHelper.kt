package com.wbrawner.pihelper.shared

import com.wbrawner.plausible.android.Plausible

object PlausibleAnalyticsHelper : AnalyticsHelper {
    override fun pageView(route: Route) {
        Plausible.pageView(route.path)
    }

    override fun event(event: AnalyticsEvent, route: Route) {
        val props = when (event) {
            is AnalyticsEvent.DisableButtonClicked -> mapOf("duration" to event.duration)
            is AnalyticsEvent.LinkClicked -> mapOf("link" to event.link)
            else -> null
        }
        Plausible.event(event.name, route.path, props = props)
    }
}