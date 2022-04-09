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
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.internal.caching.MessageCacheManager

interface ThreadCacheEntry : Thread, GuildMessageChannelCacheEntry {

    /**
     * Contains useful data like creationDate, archiveDate etc.
     */
    val metadata: Thread.ThreadMetadata

}

@PublishedApi
internal class ThreadCacheEntryImpl(
    override val guild: Guild,
    override val permissionOverwrites: Set<PermissionOverwrite>,
    override val slowModeTime: TimeSpan,
    override val parent: GuildTextChannel,
    override val id: Snowflake,
    override val name: String,
    override val type: ChannelType,
    override val metadata: Thread.ThreadMetadata,
) : ThreadCacheEntry {

    override val cacheManager = MessageCacheManager(client)

    override fun toString(): String = "ThreadCacheEntry(id=$id, type=$type)"
    override fun equals(other: Any?): Boolean = other is Thread && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}