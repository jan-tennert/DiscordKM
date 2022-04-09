/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.entities.modifier.guild.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.modifier.guild.TextChannelModifier
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import kotlinx.serialization.json.JsonObject

sealed interface TextChannel : GuildTextChannel {

    override val type: ChannelType
        get() = ChannelType.GUILD_TEXT
    override val cache: TextChannelCacheEntry?
        get() = guild.cache?.cacheManager?.channelCache?.get(id) as? TextChannelCacheEntry

    companion object : GuildChannelBuilder<TextChannelModifier, TextChannel> {
        override fun createChannel(modifier: TextChannelModifier.() -> Unit) = TextChannelModifier().apply { convertToTextChannel(); modifier(this) }

        operator fun invoke(id: Snowflake, guild: Guild): TextChannel = TextChannelImpl(id, guild)
        operator fun invoke(data: JsonObject, guild: Guild) = ChannelSerializer.deserializeChannel<TextChannelCacheEntry>(data, guild)
    }

}

internal class TextChannelImpl(override val id: Snowflake, override val guild: Guild) : TextChannel {

    override fun toString(): String = "TextChannel(id=$id, type=$type)"
    override fun equals(other: Any?): Boolean = other is TextChannel && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}