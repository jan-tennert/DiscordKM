package io.github.jan.discordkm.api.entities.channels.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.channels.PermissionOverwrite
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.internal.caching.MessageCacheManager
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import kotlinx.serialization.json.JsonObject

interface TextChannel : GuildTextChannel {

    override val type: ChannelType
        get() = ChannelType.GUILD_TEXT
    override val cache: TextChannelCacheEntry?
        get() = guild.cache?.cacheManager?.channelCache?.get(id) as? TextChannelCacheEntry

    companion object {
        operator fun invoke(id: Snowflake, guild: Guild) = guild.client.channels[id] as? TextChannelCacheEntry ?:  object : TextChannel {
            override val guild = guild
            override val id = id
        }
        operator fun invoke(data: JsonObject, guild: Guild) = ChannelSerializer.deserializeChannel<TextChannelCacheEntry>(data, guild)
    }

}

class TextChannelCacheEntry(
    override val guild: Guild,
    override val position: Int,
    override val permissionOverwrites: Set<PermissionOverwrite>,
    override val slowModeTime: TimeSpan,
    override val isNSFW: Boolean,
    override val topic: String?,
    override val defaultAutoArchiveDuration: Thread.ThreadDuration,
    override val parent: Category?,
    override val id: Snowflake,
    override val lastMessage: Message?,
    override val name: String
) : TextChannel, GuildTextChannelCacheEntry {

    override val cacheManager = MessageCacheManager(client)

}