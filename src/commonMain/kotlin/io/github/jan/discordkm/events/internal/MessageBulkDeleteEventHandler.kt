package io.github.jan.discordkm.events.internal

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.events.MessageBulkDeleteEvent
import io.github.jan.discordkm.utils.getOrThrow
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