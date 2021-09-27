package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.api.events.MessageBulkDeleteEvent
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

class MessageBulkDeleteEventHandler(val client: Client) : InternalEventHandler<MessageBulkDeleteEvent> {

    override fun handle(data: JsonObject): MessageBulkDeleteEvent {
        val channelId = data.getOrThrow<Snowflake>("channel_id")
        val channel = (client.channels[channelId] ?: client.threads[channelId]) as MessageChannel
        return MessageBulkDeleteEvent(client, data.getValue("ids").jsonArray.map { Snowflake(it.jsonPrimitive.long) }, channel)
    }

}