/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.api.entities.guild.cacheManager
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

sealed interface Category : GuildChannel, Modifiable<CategoryModifier, CategoryCacheEntry> {

    override val type: ChannelType
        get() = ChannelType.GUILD_CATEGORY
    override val cache: CategoryCacheEntry?
        get() = guild.cache?.cacheManager?.channelCache?.get(id) as? CategoryCacheEntry

    /*
     * Returns a list of guild channel objects of cached channels who have this category as their parent
     */
    val children: List<GuildChannelCacheEntry>
        get() = guild.cache?.channels?.filter { it is ParentChannel && it.parent?.id == id } ?: emptyList()

    override suspend fun modify(reason: String?, modifier: CategoryModifier.() -> Unit) = client.buildRestAction<CategoryCacheEntry> {
        route = Route.Channel.MODIFY_CHANNEL(id).patch(CategoryModifier().apply(modifier).data)
        this.reason = reason
        transform { ChannelSerializer.deserializeChannel(it.toJsonObject(), guild) }
    }

    companion object : GuildChannelBuilder<CategoryModifier, Category> {
        override fun createChannel(modifier: CategoryModifier.() -> Unit) = CategoryModifier().apply(modifier)

        operator fun invoke(id: Snowflake, guild: Guild): Category = CategoryImpl(id, guild)
        operator fun invoke(data: JsonObject, guild: Guild) = ChannelSerializer.deserializeChannel<CategoryCacheEntry>(data, guild)
    }

}

internal class CategoryImpl(override val id: Snowflake, override val guild: Guild) : Category {

    override fun toString(): String = "Category(id=$id, type=$type)"
    override fun equals(other: Any?): Boolean = other is Category && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}

class CategoryCacheEntry(
    override val guild: Guild,
    override val position: Int,
    override val permissionOverwrites: Set<PermissionOverwrite>,
    override val id: Snowflake,
    override val name: String
) : Category, GuildChannelCacheEntry, IPositionable {

    override fun toString(): String = "CategoryCacheEntry(id=$id, type=$type)"
    override fun equals(other: Any?): Boolean = other is Category && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}