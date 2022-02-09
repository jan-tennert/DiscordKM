/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.containers.OptionContainer
import io.github.jan.discordkm.api.entities.interactions.ApplicationCommandInteraction
import io.github.jan.discordkm.api.entities.messages.Message

interface CommandEvent : InteractionCreateEvent {
    val commandName: String
    override val interaction: ApplicationCommandInteraction
}

/**
 * Sent when someone enters the slash command
 */
class SlashCommandEvent(override val client: Client, override val interaction: ApplicationCommandInteraction, override val commandName: String, val options: OptionContainer, val subCommand: String? = null, val subCommandGroup: String? = null) :
    CommandEvent

/**
 * Sent when someone clicks on a message context menu command
 */
class MessageCommandEvent(override val client: Client, override val interaction: ApplicationCommandInteraction, override val commandName: String, val targetMessage: Message) :
    CommandEvent

/**
 * Sent when someone clicks on a user context menu command
 */
class UserCommandEvent(override val client: Client, override val interaction: ApplicationCommandInteraction, override val commandName: String, val targetUser: UserCacheEntry) :
    CommandEvent
