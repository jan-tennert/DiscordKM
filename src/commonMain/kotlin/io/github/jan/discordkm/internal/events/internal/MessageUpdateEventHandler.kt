package io.github.jan.discordkm.internal.events.internal

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.internal.entities.channels.MessageChannelData
import io.github.jan.discordkm.internal.entities.guilds.channels.GuildTextChannelData
import io.github.jan.discordkm.api.events.MessageUpdateEvent
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class MessageUpdateEventHandler(val client: Client) : InternalEventHandler<MessageUpdateEvent> {

    override fun handle(data: JsonObject): MessageUpdateEvent {
        val channelId = data.getOrThrow<Snowflake>("channel_id")
        val channel = (client.channels[channelId] ?: client.threads[channelId]) as MessageChannel

        val messageId = data.getOrThrow<Snowflake>("id")
        val message = Message(channel, data)
        if(channel is MessageChannelData) channel.messageCache[message.id] = message
        if(channel is GuildTextChannelData) channel.messageCache[message.id] = message
        return MessageUpdateEvent(client, message, messageId, channelId, channel)
    }

}