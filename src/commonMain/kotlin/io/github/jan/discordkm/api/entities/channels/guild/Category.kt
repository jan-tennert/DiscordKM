package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.api.entities.modifiers.Modifiable
import io.github.jan.discordkm.api.entities.modifiers.guild.CategoryModifier
import io.github.jan.discordkm.api.entities.modifiers.guild.GuildChannelBuilder
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject

interface Category : GuildChannel, Modifiable<CategoryModifier, CategoryCacheEntry> {

    override val type: ChannelType
        get() = ChannelType.GUILD_CATEGORY
    override val cache: CategoryCacheEntry?
        get() = guild.cache?.cacheManager?.channelCache?.get(id) as? CategoryCacheEntry

    override suspend fun modify(modifier: CategoryModifier.() -> Unit) = client.buildRestAction<CategoryCacheEntry> {
        route = Route.Channel.MODIFY_CHANNEL(id).patch(CategoryModifier().apply(modifier).data)
        transform { ChannelSerializer.deserializeChannel(it.toJsonObject(), guild) }
    }

    companion object : GuildChannelBuilder<CategoryModifier, Category> {
        override fun create(modifier: CategoryModifier.() -> Unit) = CategoryModifier().apply(modifier)

        operator fun invoke(id: Snowflake, guild: Guild) = guild.client.channels[id] as? CategoryCacheEntry ?: object : Category {
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