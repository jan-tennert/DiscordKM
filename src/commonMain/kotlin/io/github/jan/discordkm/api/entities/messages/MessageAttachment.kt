package io.github.jan.discordkm.api.entities.messages

import io.github.jan.discordkm.api.entities.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageAttachment(
    val id: Snowflake,
    @SerialName("filename")
    val fileName: String,
    @SerialName("content_type")
    val contentType: String? = null,
    val size: Int,
    val url: String,
    @SerialName("proxy_url")
    val proxyUrl: String,
    val height: Int? = null,
    val width: Int? = null,
    @SerialName("ephemeral")
    val isEphemeral: Boolean = false
)