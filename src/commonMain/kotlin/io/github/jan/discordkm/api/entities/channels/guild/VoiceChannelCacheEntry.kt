package io.github.jan.discordkm.api.entities.channels.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.internal.caching.MessageCacheManager

sealed interface VoiceChannelCacheEntry : VoiceChannel, GuildChannelCacheEntry, IPositionable, GuildMessageChannelCacheEntry {

    /**
     * The maximum amount of users allowed in this voice channel
     */
    val userLimit: Int

    /**
     * The id of the region this voice channel is in
     */
    val regionId: String?

    /**
     * The bitrate of the voice channel
     */
    val bitrate: Int

    /**
     * The video quality mode of the voice channel
     */
    val videoQualityMode: VoiceChannel.VideoQualityMode

    override val slowModeTime: TimeSpan
        get() = throw UnsupportedOperationException("A text channel in a voice chanel cannot have a slow mode enabled")

}

@PublishedApi
internal class VoiceChannelCacheEntryImpl(
    override val userLimit: Int,
    override val regionId: String?,
    override val bitrate: Int,
    override val videoQualityMode: VoiceChannel.VideoQualityMode,
    override val guild: Guild,
    override val id: Snowflake,
    override val name: String,
    override val position: Int,
    override val permissionOverwrites: Set<PermissionOverwrite>,
    override val parent: Category?
) : VoiceChannelCacheEntry {

    override val cacheManager = MessageCacheManager(guild.client)

    override fun toString(): String = "VoiceChannelCacheEntry(id=$id, type=$type, name=$name)"
    override fun equals(other: Any?): Boolean = other is VoiceChannel && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}