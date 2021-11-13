package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.channels.Channel
import io.github.jan.discordkm.api.entities.channels.ChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.channels.PermissionOverwrite

sealed interface GuildChannel : Channel {

    val guild: Guild

    /**
     * Whether this guild channel is a thread
     */
    val isThread: Boolean
        get() = type == ChannelType.GUILD_NEWS_THREAD || type == ChannelType.GUILD_PUBLIC_THREAD || type == ChannelType.GUILD_PRIVATE_THREAD

    override val client: Client
        get() = guild.client


    //modifier

}

sealed interface GuildChannelCacheEntry: GuildChannel, ChannelCacheEntry, Nameable {

    /**
     * The permission overrides for this channel
     */
    val permissionOverwrites: Set<PermissionOverwrite>

}