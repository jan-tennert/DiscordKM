package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.Nameable
import io.github.jan.discordkm.api.entities.channels.Channel
import io.github.jan.discordkm.api.entities.channels.ChannelCacheEntry
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.api.entities.modifiers.Modifiable
import io.github.jan.discordkm.api.entities.modifiers.guild.CategoryModifier
import io.github.jan.discordkm.api.entities.modifiers.guild.GuildChannelModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.toJsonObject

sealed interface GuildChannel : Channel {

    val guild: Guild

    /**
     * Whether this guild channel is a thread
     */
    val isThread: Boolean
        get() = type == ChannelType.GUILD_NEWS_THREAD || type == ChannelType.GUILD_PUBLIC_THREAD || type == ChannelType.GUILD_PRIVATE_THREAD

    override val client: Client
        get() = guild.client

}

sealed interface GuildChannelCacheEntry: GuildChannel, ChannelCacheEntry, Nameable {

    /**
     * The permission overrides for this channel
     */
    val permissionOverwrites: Set<PermissionOverwrite>

}