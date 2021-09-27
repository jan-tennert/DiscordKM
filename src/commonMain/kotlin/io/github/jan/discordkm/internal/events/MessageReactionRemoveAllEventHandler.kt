package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.api.events.MessageReactionRemoveAllEvent
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
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