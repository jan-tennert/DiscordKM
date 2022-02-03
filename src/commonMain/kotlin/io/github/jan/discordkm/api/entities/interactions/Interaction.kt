/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.DiscordLocale
import io.github.jan.discordkm.api.entities.Mentionable
import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.channels.Channel
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.MemberCacheEntry
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.interactions.commands.CommandOption
import io.github.jan.discordkm.api.entities.messages.MessageAttachment
import io.github.jan.discordkm.internal.DiscordKMUnstable
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import io.github.jan.discordkm.internal.utils.get
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.locale
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject


open class Interaction(override val client: Client, override val data: JsonObject) : SerializableEntity {

    /**
     * The interaction token
     */
    val token: String get() = data["token"]!!.string

    /**
     * The [InteractionType]
     */
    val type: InteractionType get() = InteractionType[data["type"]!!.int]

    /**
     * The interaction id
     */
    val id: Snowflake get() = data["id"]!!.snowflake

    /**
     * The application id
     */
    val applicationId: Snowflake get() = data["application_id"]!!.snowflake

    /**
     * The guild id, if this was sent in a guild
     */
    val guild: Guild? get() = data["guild_id", true]?.snowflake?.let { Guild(it, client) }

    /**
     * The member, if a guild member was involved in this interaction
     */
    val member: MemberCacheEntry? get() = data["member"]?.let { Member(it.jsonObject, guild!!) }

    /**
     * The channel id, if this interaction was sent in a channel
     */
    val channel: MessageChannel?
        get() = data["channel_id", true]?.snowflake?.let {
            Channel(
                it,
                ChannelType.UNKNOWN,
                client,
                guild
            )
        } as? MessageChannel

    /**
     * The user, if this interaction was sent in a private channel
     */
    val user: User get() = data["user"]?.let { User(it.jsonObject, client) } ?: member!!.user

    /**
     * The selected language of the user who invoked this interaction
     */
    val locale: DiscordLocale? get() = data["locale", true]?.locale

    /**
     * The preferred locale of the guild, if invoked in a guild
     */
    val guildLocale: DiscordLocale? get() = data["guild_locale", true]?.locale


    /**
     * Whether this interaction was already acknowledged
     */
    var isAcknowledged: Boolean = false
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

    @DiscordKMUnstable
    val attachment: MessageAttachment
        get() = value as MessageAttachment

    inline fun <reified T> asEnum() where T : EnumWithValue<*>, T : Enum<T> = enumValues<T>().first { it.value == value }

}

enum class InteractionType : EnumWithValue<Int> {
    PING,
    APPLICATION_COMMAND,
    MESSAGE_COMPONENT,
    APPLICATION_COMMAND_AUTOCOMPLETE,
    @DiscordKMUnstable MODAL_SUBMIT;

    override val value: Int
        get() = ordinal + 1

    companion object : EnumWithValueGetter<InteractionType, Int>(values())
}