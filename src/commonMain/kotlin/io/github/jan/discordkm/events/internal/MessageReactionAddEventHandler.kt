package io.github.jan.discordkm.events.internal

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.entities.guild.Emoji
import io.github.jan.discordkm.entities.guild.Member
import io.github.jan.discordkm.events.MessageReactionAddEvent
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
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