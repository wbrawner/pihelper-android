package com.wbrawner.pihelper.shared

interface AnalyticsHelper {
    fun pageView(route: Route)
    fun event(event: AnalyticsEvent, route: Route)

    companion object
}

sealed class AnalyticsEvent(val name: String) {
    object ScanButtonClick : AnalyticsEvent("Scan button clicked")
    object ConnectButtonClick : AnalyticsEvent("Connect button clicked")
    object AuthenticateWithPasswordButtonClicked
        : AnalyticsEvent("Authenticate with password button clicked")

    object AuthenticateWithApiKeyButtonClicked
        : AnalyticsEvent("Authenticate with API key button clicked")

    object EnableButtonClicked : AnalyticsEvent("Enable button clicked")
    data class DisableButtonClicked(val duration: Long?) : AnalyticsEvent("Disable button clicked")
    object ForgetButtonClicked : AnalyticsEvent("Forget button clicked")
    data class LinkClicked(val link: String) : AnalyticsEvent("Link clicked")
}