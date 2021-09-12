package io.github.jan.discordkm.events.internal

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.events.MessageDeleteEvent
import io.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class MessageDeleteEventHandler(val client: Client) : InternalEventHandler<MessageDeleteEvent> {

    override fun handle(data: JsonObject): MessageDeleteEvent {
        val channelId = data.getOrThrow<Snowflake>("channel_id")
        val channel = (client.channels[channelId] ?: client.threads[channelId]) as MessageChannel
        return MessageDeleteEvent(client, data.getOrThrow("id"), channel)
    }

}