package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.Channel
import io.github.jan.discordkm.api.entities.channels.ChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite

sealed interface GuildChannel : Channel {

    val guild: Guild

    /**
     * Whether this guild channel is a thread
     */
    val isThread: Boolean
        get() = type == ChannelType.GUILD_NEWS_THREAD || type == ChannelType.GUILD_PUBLIC_THREAD || type == ChannelType.GUILD_PRIVATE_THREAD

    override val client: Client
        get() = guild.client

    //edit permissions?

}

internal class GuildChannelImpl(override val id: Snowflake, override val guild: Guild) : GuildChannel {

    override val cache: GuildChannelCacheEntry?
        get() = guild.cache?.channels?.get(id)

    override val type: ChannelType
        get() = cache?.type ?: ChannelType.UNKNOWN

    override fun toString(): String = "GuildChannel(id=$id, type=$type)"
    override fun equals(other: Any?): Boolean = other is GuildChannel && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}

interface GuildChannelCacheEntry: GuildChannel, ChannelCacheEntry, Nameable {

    /**
     * The permission overrides for this channel
     */
    val permissionOverwrites: Set<PermissionOverwrite>

}