package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.events.MessageUpdateEvent
import io.github.jan.discordkm.internal.Cache
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.internal.entities.channels.MessageChannelData
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class MessageUpdateEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<MessageUpdateEvent> {

    override fun handle(data: JsonObject): MessageUpdateEvent {
        val channelId = data.getOrThrow<Snowflake>("channel_id")
        val channel = (client.channels[channelId] ?: client.threads[channelId] ?: MessageChannelData.fromId(client, channelId)) as MessageChannel
        val messageId = data.getOrThrow<Snowflake>("id")
        val message = Message(channel, data)
        if(Cache.MESSAGES in client.enabledCache) channel.messageCache[message.id] = message
        return MessageUpdateEvent(client, message, messageId, channelId, channel)
    }

}