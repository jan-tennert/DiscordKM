package io.github.jan.discordkm.internal.entities.channels

import io.github.jan.discordkm.Cache
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.lists.MessageList
import io.github.jan.discordkm.api.entities.messages.Message
import kotlinx.serialization.json.JsonObject

open class MessageChannelData(override val client: Client, override val data: JsonObject) : MessageChannel {

    val messageCache: Cache<Message> = Cache.fromSnowflakeEntityList(emptyList())
    override val messages: MessageList
        get() = MessageList(this, messageCache.values)

}