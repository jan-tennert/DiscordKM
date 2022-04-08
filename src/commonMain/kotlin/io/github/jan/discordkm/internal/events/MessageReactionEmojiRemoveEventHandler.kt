/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.events.MessageReactionEmojiRemoveEvent
import io.github.jan.discordkm.internal.serialization.serializers.GuildSerializer
import io.github.jan.discordkm.internal.utils.get
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.snowflake
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal class MessageReactionEmojiRemoveEventHandler(val client: DiscordClient) : InternalEventHandler<MessageReactionEmojiRemoveEvent> {

    override suspend fun handle(data: JsonObject): MessageReactionEmojiRemoveEvent {
        val channel = MessageChannel(data["channel_id"]!!.snowflake, client)
        val emojiObject = data.getValue("emoji").jsonObject
        val guild = data["guild_id", true]?.snowflake?.let { Guild(it, client) }
        val emoji = if(emojiObject.getOrNull<Snowflake>("id") == null) {
            Emoji.fromUnicode(emojiObject.getOrThrow("name"))
        } else {
            Emoji.fromEmote(GuildSerializer.deserializeGuildEmote(emojiObject, client))
        }
        val message = Message(data["message_id"]!!.snowflake, channel)
        return MessageReactionEmojiRemoveEvent(client, channel, emoji, guild, message)
    }

}