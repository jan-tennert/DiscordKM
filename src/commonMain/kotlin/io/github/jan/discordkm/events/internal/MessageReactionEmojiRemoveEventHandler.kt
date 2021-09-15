package io.github.jan.discordkm.events.internal

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.entities.guild.Emoji
import io.github.jan.discordkm.events.MessageReactionEmojiRemoveEvent
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class MessageReactionEmojiRemoveEventHandler(val client: Client) : InternalEventHandler<MessageReactionEmojiRemoveEvent> {

    override fun handle(data: JsonObject): MessageReactionEmojiRemoveEvent {
        val channelId = data.getOrThrow<Snowflake>("channel_id")
        val channel = (client.channels[channelId] ?: client.threads[channelId]) as MessageChannel
        val messageId = data.getOrThrow<Snowflake>("message_id")
        val emojiObject = data.getValue("emoji").jsonObject
        val emoji = if(emojiObject.getOrNull<Snowflake>("id") == null) {
            Emoji.fromEmoji(emojiObject.getOrThrow("name"))
        } else {
            Emoji.fromEmote(Emoji.Emote(emojiObject, client))
        }
        val guildId = data.getOrNull<Snowflake>("guild_id")
        return MessageReactionEmojiRemoveEvent(client, messageId, channelId, channel, emoji, guildId)
    }

}