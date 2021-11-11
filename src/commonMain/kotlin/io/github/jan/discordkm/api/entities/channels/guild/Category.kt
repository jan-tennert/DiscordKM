package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.channels.PermissionOverwrite
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import kotlinx.serialization.json.JsonObject

interface Category : GuildChannel {

    override val type: ChannelType
        get() = ChannelType.GUILD_CATEGORY

    companion object {
        fun from(id: Snowflake, guild: Guild) = object : Category {
            override val guild = guild
            override val id = id
        }
        fun from(data: JsonObject, guild: Guild) = ChannelSerializer.deserializeChannel<CategoryCacheEntry>(data, guild)
    }

}

class CategoryCacheEntry(
    override val guild: Guild,
    override val position: Int,
    override val permissionOverwrites: Set<PermissionOverwrite>,
    override val id: Snowflake,
    override val name: String
) : Category, GuildChannelCacheEntry, IPositionable