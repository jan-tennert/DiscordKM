package io.github.jan.discordkm.api.entities.channels.guild
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.internal.caching.MessageCacheManager

sealed interface StageChannelCacheEntry : StageChannel, VoiceChannelCacheEntry

@PublishedApi
internal class StageChannelCacheEntryImpl(
    override val userLimit: Int,
    override val regionId: String?,
    override val bitrate: Int,
    override val videoQualityMode: VoiceChannel.VideoQualityMode,
    override val guild: Guild,
    override val id: Snowflake,
    override val name: String,
    override val position: Int,
    override val permissionOverwrites: Set<PermissionOverwrite>,
    override val parent: Category?,
) : StageChannelCacheEntry {

    override val cacheManager = MessageCacheManager(guild.client)

    override fun toString(): String = "StageChannelCacheEntry(id=$id, type=$type, name=$name)"
    override fun equals(other: Any?): Boolean = other is StageChannel && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}