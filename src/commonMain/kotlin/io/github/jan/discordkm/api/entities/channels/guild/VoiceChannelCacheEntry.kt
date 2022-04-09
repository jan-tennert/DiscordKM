/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.channels.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.internal.caching.MessageCacheManager

sealed interface VoiceChannelCacheEntry : VoiceChannel, GuildChannelCacheEntry, IPositionable, GuildMessageChannelCacheEntry {

    /**
     * The maximum amount of users allowed in this voice channel
     */
    val userLimit: Int

    /**
     * The id of the region this voice channel is in
     */
    val regionId: String?

    /**
     * The bitrate of the voice channel
     */
    val bitrate: Int

    /**
     * The video quality mode of the voice channel
     */
    val videoQualityMode: VoiceChannel.VideoQualityMode

    override val slowModeTime: TimeSpan
        get() = throw UnsupportedOperationException("A text channel in a voice chanel cannot have a slow mode enabled")

}

@PublishedApi
internal class VoiceChannelCacheEntryImpl(
    override val userLimit: Int,
    override val regionId: String?,
    override val bitrate: Int,
    override val videoQualityMode: VoiceChannel.VideoQualityMode,
    override val guild: Guild,
    override val id: Snowflake,
    override val name: String,
    override val position: Int,
    override val permissionOverwrites: Set<PermissionOverwrite>,
    override val parent: Category?
) : VoiceChannelCacheEntry {

    override val cacheManager = MessageCacheManager(guild.client)

    override fun toString(): String = "VoiceChannelCacheEntry(id=$id, type=$type, name=$name)"
    override fun equals(other: Any?): Boolean = other is VoiceChannel && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}