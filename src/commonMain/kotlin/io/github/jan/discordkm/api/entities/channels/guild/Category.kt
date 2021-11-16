package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.channels.PermissionOverwrite
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import kotlinx.serialization.json.JsonObject

interface Category : GuildChannel {

    override val type: ChannelType
        get() = ChannelType.GUILD_CATEGORY
    override val cache: CategoryCacheEntry?
        get() = guild.cache?.cacheManager?.channelCache?.get(id) as? CategoryCacheEntry

    companion object {
        operator fun invoke(id: Snowflake, guild: Guild) = guild.client.channels[id] ?: object : Category {
            override val guild = guild
            override val id = id
        }
        operator fun invoke(data: JsonObject, guild: Guild) = ChannelSerializer.deserializeChannel<CategoryCacheEntry>(data, guild)
    }

}

class CategoryCacheEntry(
    override val guild: Guild,
    override val position: Int,
    override val permissionOverwrites: Set<PermissionOverwrite>,
    override val id: Snowflake,
    override val name: String
) : Category, GuildChannelCacheEntry, IPositionable