package io.github.jan.discordkm.internal.events.internal

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.events.MessageDeleteEvent
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class MessageDeleteEventHandler(val client: Client) : InternalEventHandler<MessageDeleteEvent> {

    override fun handle(data: JsonObject): MessageDeleteEvent {
        val channelId = data.getOrThrow<Snowflake>("channel_id")
        val channel = (client.channels[channelId] ?: client.threads[channelId]) as MessageChannel
        val messageId = data.getOrThrow<Snowflake>("id")
        channel.messageCache.remove(messageId)
        return MessageDeleteEvent(client, messageId, channel)
    }

}