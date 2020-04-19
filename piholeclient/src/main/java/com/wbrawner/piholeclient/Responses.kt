package com.wbrawner.piholeclient

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Summary(
    @Json(name = "domains_being_blocked")
    val domainsBeingBlocked: String,
    @Json(name = "dns_queries_today")
    val dnsQueriesToday: String,
    @Json(name = "ads_blocked_today")
    val adsBlockedToday: String,
    @Json(name = "ads_percentage_today")
    val adsPercentageToday: String,
    @Json(name = "unique_domains")
    val uniqueDomains: String,
    @Json(name = "queries_forwarded")
    val queriesForwarded: String,
    @Json(name = "clients_ever_seen")
    val clientsEverSeen: String,
    @Json(name = "unique_clients")
    val uniqueClients: String,
    @Json(name = "dns_queries_all_types")
    val dnsQueriesAllTypes: String,
    @Json(name = "queries_cached")
    val queriesCached: String,
    @Json(name = "no_data_replies")
    val noDataReplies: String?,
    @Json(name = "nx_domain_replies")
    val nxDomainReplies: String?,
    @Json(name = "cname_replies")
    val cnameReplies: String?,
    @Json(name = "in_replies")
    val ipReplies: String?,
    @Json(name = "privacy_level")
    val privacyLevel: String,
    override val status: Status,
    @Json(name = "gravity_last_updated")
    val gravity: Gravity?,
    val type: String?,
    val version: Int?
) : StatusProvider

@Keep
enum class Status {
    @Json(name = "enabled")
    ENABLED,
    @Json(name = "disabled")
    DISABLED
}

@JsonClass(generateAdapter = true)
data class Gravity(
    @Json(name = "file_exists")
    val fileExists: Boolean,
    val absolute: Int,
    val relative: Relative
)

@JsonClass(generateAdapter = true)
data class Relative(
    val days: String,
    val hours: String,
    val minutes: String
)

@JsonClass(generateAdapter = true)
data class VersionResponse(val version: Int)

@JsonClass(generateAdapter = true)
data class TopItemsResponse(
    @Json(name = "top_queries") val topQueries: Map<String, String>,
    @Json(name = "top_ads") val topAds: Map<String, Double>
)

@JsonClass(generateAdapter = true)
data class StatusResponse(
    override val status: Status
) : StatusProvider

interface StatusProvider {
    val status: Status
}
