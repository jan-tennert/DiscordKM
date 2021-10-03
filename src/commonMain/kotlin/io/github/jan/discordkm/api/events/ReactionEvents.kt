/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.Intent
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.internal.entities.channels.MessageChannel


/**
 * Sent when someone removes a reaction
 *
 * Requires the intent [Intent.GUILD_MESSAGE_REACTIONS] or [Intent.DIRECT_MESSAGE_REACTIONS]
 */
class MessageReactionRemoveEvent(
    override val client: Client,
    override val channel: MessageChannel,
    override val messageId: Snowflake,
    val emoji: Emoji,
    override val channelId: Snowflake,
    val userId: Snowflake,
    val user: User,
    val guildId: Snowflake?
) : MessageEvent {


    suspend fun retrieveGuild() = client.guilds.retrieve(guildId!!)

}

/**
 * Sent when someone removes all messages from a message
 *
 * Requires the intent [Intent.GUILD_MESSAGE_REACTIONS] or [Intent.DIRECT_MESSAGE_REACTIONS]
 */
class MessageReactionRemoveAllEvent(
    override val client: Client,
    override val messageId: Snowflake,
    override val channelId: Snowflake,
    override val channel: MessageChannel,
    val guildId: Snowflake?
) : MessageEvent {


    suspend fun retrieveGuild() = client.guilds.retrieve(guildId!!)

}

/**
 * Sent when every reaction with the same emojis are deleted
 *
 * Requires the intent [Intent.GUILD_MESSAGE_REACTIONS] or [Intent.DIRECT_MESSAGE_REACTIONS]
 */
class MessageReactionEmojiRemoveEvent(
    override val client: Client,
    override val messageId: Snowflake,
    override val channelId: Snowflake,
    override val channel: MessageChannel,
    val emoji: Emoji,
    val guildId: Snowflake?,
) : MessageEvent {


    suspend fun retrieveGuild() = client.guilds.retrieve(guildId!!)

}

/**
 * Sent when reacts to a message
 *
 * Requires the intent [Intent.GUILD_MESSAGE_REACTIONS] or [Intent.DIRECT_MESSAGE_REACTIONS]
 */
class MessageReactionAddEvent(
    override val client: Client,
    override val channel: MessageChannel,
    override val messageId: Snowflake,
    val emoji: Emoji,
    override val channelId: Snowflake,
    val userId: Snowflake,
    val user: User,
    val member: Member?,
    val guildId: Snowflake?
) : MessageEvent {


    suspend fun retrieveGuild() = client.guilds.retrieve(guildId!!)

}