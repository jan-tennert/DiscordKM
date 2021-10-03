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
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.Intent
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.internal.entities.channels.MessageChannel

/**
 * Sent when someone sends a message into a message channel
 *
 * Requires the intent [Intent.GUILD_MESSAGES] or [Intent.DIRECT_MESSAGES]
 */
class MessageCreateEvent(
    override val client: Client, val message: Message,
    override val messageId: Snowflake,
    override val channelId: Snowflake,
    override val channel: MessageChannel
) : MessageEvent

/**
 * Sent when someone deletes a message
 *
 * Requires the intent [Intent.GUILD_MESSAGES] or [Intent.DIRECT_MESSAGES]
 */
class MessageDeleteEvent(override val client: Client, val id: Snowflake, val channel: MessageChannel) : Event

/**
 * Sent when someone deletes multiple messages at once
 *
 * Requires the intent [Intent.GUILD_MESSAGES] or [Intent.DIRECT_MESSAGES]
 */
class MessageBulkDeleteEvent(override val client: Client, val ids: List<Snowflake>, val channel: MessageChannel) : Event