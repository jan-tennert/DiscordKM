package io.github.jan.discordkm.internal.events.internal

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.events.MessageReactionAddEvent
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class MessageReactionAddEventHandler(val client: Client) : InternalEventHandler<MessageReactionAddEvent> {
    override fun handle(data: JsonObject): MessageReactionAddEvent {
        val channelId = data.getOrThrow<Snowflake>("channel_id")
        val channel = (client.channels[channelId] ?: client.threads[channelId]) as MessageChannel
        val emojiObject = data.getValue("emoji").jsonObject
        val emoji = if(emojiObject.getOrNull<Snowflake>("id") == null) {
            Emoji.fromEmoji(emojiObject.getOrThrow("name"))
        } else {
            Emoji.fromEmote(Emoji.Emote(emojiObject, client))
        }
        val userId = data.getOrThrow<Snowflake>("user_id")
        val user = client.users[userId]!!
        val messageId = data.getOrThrow<Snowflake>("message_id")
        val member = data.getOrNull<Member>("member")
        val guildId = data.getOrNull<Snowflake>("guild_id")
        return MessageReactionAddEvent(client, channel, messageId, emoji, channelId, userId, user, member, guildId)
    }
}