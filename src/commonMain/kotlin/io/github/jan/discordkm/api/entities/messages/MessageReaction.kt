/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.messages

import io.github.jan.discordkm.api.entities.guild.Emoji
import kotlinx.serialization.Serializable

/*
 * Represents a reaction of a message
 *
 * @param count The amount of users that have reacted to the message
 * @param me Whether the bot has reacted to the message
 * @param emoji The reaction emoji
 */
@Serializable
data class MessageReaction(
    val count: Int,
    val me: Boolean,
    val emoji: Emoji
)