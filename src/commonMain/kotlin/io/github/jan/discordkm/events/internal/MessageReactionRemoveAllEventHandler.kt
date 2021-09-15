package io.github.jan.discordkm.events.internal

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.events.MessageReactionRemoveAllEvent
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class MessageReactionRemoveAllEventHandler(val client: Client) : InternalEventHandler<MessageReactionRemoveAllEvent> {

    override fun handle(data: JsonObject): MessageReactionRemoveAllEvent {
        val channelId = data.getOrThrow<Snowflake>("channel_id")
        val channel = (client.channels[channelId] ?: client.threads[channelId]) as MessageChannel
        val messageId = data.getOrThrow<Snowflake>("message_id")
        val guildId = data.getOrNull<Snowflake>("guild_id")
        return MessageReactionRemoveAllEvent(client, messageId, channelId, channel, guildId)
    }

}