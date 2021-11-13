/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.Mentionable
import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.channels.Channel
import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.MemberCacheEntry
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.interactions.commands.CommandOption
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter

open class Interaction(
    override val client: Client,
    override val id: Snowflake,
    val applicationId: Snowflake,
    val type: InteractionType,
    val guild: Guild?,
    val channel: MessageChannel,
    val member: MemberCacheEntry?,
    val user: UserCacheEntry,
    val token: String,
    val version: Int
) : SnowflakeEntity, BaseEntity {

    var isAcknowledged = false
        internal set

}

class InteractionOption(override val name: String, val type: CommandOption.OptionType, val value: Any) : Nameable {

    val user: User
        get() = value as User

    val int: Int
        get() = value as Int

    val double: Double
        get() = value as Double

    val channel: Channel
        get() = value as Channel

    val mentionable: Mentionable
        get() = value as Mentionable

    val role: Role
        get() = value as Role

    val string: String
        get() = value.toString()

    val boolean: Boolean
        get() = value as Boolean

}

enum class InteractionType : EnumWithValue<Int> {
    PING,
    APPLICATION_COMMAND,
    MESSAGE_COMPONENT,
    APPLICATION_COMMAND_AUTOCOMPLETE;

    override val value: Int
        get() = ordinal + 1

    companion object : EnumWithValueGetter<InteractionType, Int>(values())
}