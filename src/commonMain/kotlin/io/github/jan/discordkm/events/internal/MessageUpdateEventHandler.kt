package io.github.jan.discordkm.events.internal

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.entities.messages.Message
import io.github.jan.discordkm.events.MessageUpdateEvent
import io.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class MessageUpdateEventHandler(val client: Client) : InternalEventHandler<MessageUpdateEvent> {

    override fun handle(data: JsonObject): MessageUpdateEvent {
        val channelId = data.getOrThrow<Snowflake>("channel_id")
        val channel = (client.channels[channelId] ?: client.threads[channelId]) as MessageChannel

        val messageId = data.getOrThrow<Snowflake>("id")
        val message = Message(channel, data)
        channel.messageCache[message.id] = message
        return MessageUpdateEvent(client, message, messageId, channelId, channel)
    }

}