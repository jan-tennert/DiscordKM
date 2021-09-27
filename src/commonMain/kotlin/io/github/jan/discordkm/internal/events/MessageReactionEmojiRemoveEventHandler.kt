package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.events.MessageReactionEmojiRemoveEvent
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
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