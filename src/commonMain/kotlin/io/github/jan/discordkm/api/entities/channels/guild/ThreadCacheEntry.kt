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