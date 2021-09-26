/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.Mentionable
import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.interactions.commands.CommandOption
import io.github.jan.discordkm.api.entities.lists.retrieve
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.internal.entities.UserData
import io.github.jan.discordkm.internal.entities.channels.Channel
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.internal.entities.guilds.MemberData
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.valueOfIndex
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

open class Interaction(override val client: Client, override val data: JsonObject) : SerializableEntity {

    /**
     * The interaction token
     */
    val token: String
        get() = data.getOrThrow("token")

    /**
     * The [InteractionType]
     */
    val type: InteractionType
        get() = valueOfIndex(data.getOrThrow("type"))

    /**
     * The interaction id
     */
    val id: Snowflake
        get() = data.getOrThrow("id")

    /**
     * The application id
     */
    val applicationId: Snowflake
        get() = data.getOrThrow("application_id")

    /**
     * The guild id, if this was sent in a guild
     */
    val guildId: Snowflake?
        get() = data.getOrNull("guild_id")

    /**
     * The member, if a guild member was involved in this interaction
     */
    val member: Member?
        get() = data["member"]?.let { MemberData(client.guilds[guildId!!]!!, it.jsonObject) }

    /**
     * The channel id, if this interaction was sent in a channel
     */
    val channelId: Snowflake
        get() = data.getOrThrow("channel_id")

    /**
     * The user, if this interaction was sent in a private channel
     */
    val user: UserData?
        get() = data["user"]?.let { UserData(client, it.jsonObject) }

    val channel: MessageChannel?
        get() = client.channels[channelId] as? MessageChannel

    /**
     * The message, if this is a ComponentInteraction
     */
    val message: Message?
        get() = data["message"]?.let { Message(channel!!, it.jsonObject) }

    /**
     * Whether this interaction was already acknowledged
     */
    var isAcknowledged: Boolean = false
        internal set

    suspend fun retrieveChannel() = client.channels.retrieve(channelId)

    class InteractionOption(val name: String, val type: CommandOption.OptionType, val value: Any) {

        val user: UserData
            get() = value as UserData

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

    enum class InteractionType {
        PING,
        APPLICATION_COMMAND,
        MESSAGE_COMPONENT,
        APPLICATION_COMMAND_AUTOCOMPLETE
    }

}