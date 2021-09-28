package io.github.jan.discordkm.internal.entities.channels

import io.github.jan.discordkm.internal.Cache
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.messages.Message
import kotlinx.serialization.json.JsonObject

open class MessageChannelData(override val client: Client, override val data: JsonObject) : MessageChannel {

    override val messageCache: Cache<Message> = Cache.fromSnowflakeEntityList(emptyList())

}