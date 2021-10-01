package io.github.jan.discordkm.internal.entities.channels

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

open class MessageChannelData(override val client: Client, override val data: JsonObject) : MessageChannel {

    companion object {
        fun fromId(client: Client, id: Snowflake) = MessageChannelData(client, buildJsonObject { put("id", id.string) })
    }

}