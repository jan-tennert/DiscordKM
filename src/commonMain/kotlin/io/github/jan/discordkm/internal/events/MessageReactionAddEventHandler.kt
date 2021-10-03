/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

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