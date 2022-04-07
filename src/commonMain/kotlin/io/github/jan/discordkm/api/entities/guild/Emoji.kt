/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild

import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.Mentionable
import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Emoji.Emote
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An emoji can be a unicode emoji or an [Emote]
 */
@Serializable
class Emoji private constructor(
    val id: Snowflake? = null,
    val name: String,
    @SerialName("animated")
    val isAnimated: Boolean = false
) : Mentionable {

    /**
     * An emote is an emoji which is only available on a specific guild
     *
     * @param id The id of the emote
     * @param name The name of the emote
     * @param isAnimated Whether the emote is animated
     * @param isAvailable Whether the emote is available on the guild (can be unavailable when the server boost tier changed)
     * @param roles The roles which can use the emote
     * @param creator The user who created the emote
     * @param requiresColons Whether this emoji must be wrapped in colons
     * @param isManagedByAnIntegration Whether this emoji is managed by an integration
     */
    class Emote internal constructor(
        override val client: DiscordClient,
        override val id: Snowflake,
        override val name: String,
        val creator: User?,
        val requiresColons: Boolean,
        val isManagedByAnIntegration: Boolean,
        val isAnimated: Boolean,
        val isAvailable: Boolean,
        val roles: List<Snowflake>
    ) : SnowflakeEntity, BaseEntity, Nameable {

        fun toEmoji() = Emoji(id, name, isAnimated)

        override fun toString(): String = "GuildEmote(id=$id, name=$name)"
        override fun equals(other: Any?): Boolean = other is Emote && other.id == id
        override fun hashCode(): Int = id.hashCode()

    }

    companion object {

        fun fromUnicode(unicode: String) = Emoji(name = unicode)

        fun fromEmote(name: String, id: Snowflake) = Emoji(name = name, id = id)

        fun fromEmote(emote: Emote) = emote.toEmoji()

    }

    override val asMention = if (id != null) "$id:$name" else name

}