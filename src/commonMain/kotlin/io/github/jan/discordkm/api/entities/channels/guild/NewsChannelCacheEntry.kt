package io.github.jan.discordkm.api.entities.channels.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.internal.caching.MessageCacheManager

sealed interface NewsChannelCacheEntry : NewsChannel, GuildTextChannelCacheEntry

@PublishedApi
internal class NewsChannelCacheEntryImpl(
    override val guild: Guild,
    override val position: Int,
    override val permissionOverwrites: Set<PermissionOverwrite>,
    override val slowModeTime: TimeSpan,
    override val isNSFW: Boolean,
    override val topic: String?,
    override val defaultAutoArchiveDuration: Thread.ThreadDuration,
    override val parent: Category?,
    override val id: Snowflake,
    override val name: String
) : NewsChannelCacheEntry {

    override val cacheManager = MessageCacheManager(client)

    override fun toString(): String = "NewsChannelCacheEntry(id=$id, type=$type, name=$name)"
    override fun equals(other: Any?): Boolean = other is NewsChannelCacheEntry && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}