package io.github.jan.discordkm.api.entities.channels.guild

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite

sealed interface CategoryCacheEntry : Category, GuildChannelCacheEntry, IPositionable {



}

@PublishedApi
internal class CategoryCacheEntryImpl(
    override val guild: Guild,
    override val position: Int,
    override val permissionOverwrites: Set<PermissionOverwrite>,
    override val id: Snowflake,
    override val name: String
) : CategoryCacheEntry {

    override fun toString(): String = "CategoryCacheEntry(id=$id, type=$type)"
    override fun equals(other: Any?): Boolean = other is Category && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}