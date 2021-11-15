package io.github.jan.discordkm.api.entities.channels.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.channels.MessageChannelCacheEntry
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.internal.entities.channels.Invitable

interface GuildMessageChannel : GuildChannel, MessageChannel {

    override val cache: GuildMessageChannelCacheEntry?

    companion object {
        operator fun invoke(id: Snowflake, guild: Guild) = object : GuildMessageChannel {
            override val guild = guild
            override val cache: GuildMessageChannelCacheEntry?
                get() = guild.cache?.channels?.get(id) as? GuildMessageChannelCacheEntry
            override val type = cache?.type ?: ChannelType.UNKNOWN
            override val id = id
        }
    }

}

sealed interface GuildMessageChannelCacheEntry : GuildMessageChannel, GuildChannelCacheEntry, MessageChannelCacheEntry, ParentChannel,
    Invitable {

    val slowModeTime: TimeSpan

}