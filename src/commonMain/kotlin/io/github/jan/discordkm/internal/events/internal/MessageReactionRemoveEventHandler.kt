package io.github.jan.discordkm.internal.events.internal

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.events.MessageReactionRemoveEvent
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class MessageReactionRemoveEventHandler(val client: Client) : InternalEventHandler<MessageReactionRemoveEvent> {

    override fun handle(data: JsonObject): MessageReactionRemoveEvent {
        val channelId = data.getOrThrow<Snowflake>("channel_id")
        val emojiObject = data.getValue("emoji").jsonObject
        val channel = (client.channels[channelId] ?: client.threads[channelId]) as MessageChannel
        val emoji = if(emojiObject.getOrNull<Snowflake>("id") == null) {
            Emoji.fromEmoji(emojiObject.getOrThrow("name"))
        } else {
            Emoji.fromEmote(Emoji.Emote(emojiObject, client))
        }
        val userId = data.getOrThrow<Snowflake>("user_id")
        val user = client.users[userId]!!
        val messageId = data.getOrThrow<Snowflake>("message_id")
        val guildId = data.getOrNull<Snowflake>("guild_id")
        return MessageReactionRemoveEvent(client, channel, messageId, emoji, channelId, userId, user, guildId)
    }

}