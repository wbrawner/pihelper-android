package com.wbrawner.pihelper.shared

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Summary(
    override val status: Status,
    val type: String? = null,
    val version: Int? = null
) : StatusProvider

@Serializable(with = Status.Serializer::class)
sealed class Status(val name: String) {
    @SerialName("enabled")
    object Enabled : Status("enabled")

    @SerialName("disabled")
    data class Disabled(val timeRemaining: String? = null) : Status(name) {
        companion object {
            const val name: String = "disabled"
        }
    }

    class Serializer : KSerializer<Status> {
        override val descriptor: SerialDescriptor
            get() = String.serializer().descriptor

        override fun deserialize(decoder: Decoder): Status {
            return when (decoder.decodeString()) {
                Enabled.name -> Enabled
                Disabled.name -> Disabled()
                else -> throw IllegalArgumentException("Invalid status")
            }
        }

        override fun serialize(encoder: Encoder, value: Status) {
            encoder.encodeString(value.name)
        }
    }
}

@Serializable()
data class VersionResponse(val version: Int)

@Serializable()
data class TopItemsResponse(
    @SerialName("top_queries") val topQueries: Map<String, String>,
    @SerialName("top_ads") val topAds: Map<String, Double>
)

@Serializable()
data class StatusResponse(
    override val status: Status
) : StatusProvider

interface StatusProvider {
    val status: Status
}
