package io.github.jan.discordkm.api.entities.channels.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.entities.modifiers.guild.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.modifiers.guild.TextChannelModifier
import io.github.jan.discordkm.internal.caching.MessageCacheManager
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import kotlinx.serialization.json.JsonObject


sealed interface TextChannel : GuildTextChannel {

    override val type: ChannelType
        get() = ChannelType.GUILD_TEXT
    override val cache: TextChannelCacheEntry?
        get() = guild.cache?.cacheManager?.channelCache?.get(id) as? TextChannelCacheEntry

    companion object : GuildChannelBuilder<TextChannelModifier, TextChannel> {
        override fun create(modifier: TextChannelModifier.() -> Unit) = TextChannelModifier().apply { convertToTextChannel(); modifier(this) }

        operator fun invoke(id: Snowflake, guild: Guild): TextChannel = TextChannelImpl(id, guild)
        operator fun invoke(data: JsonObject, guild: Guild) = ChannelSerializer.deserializeChannel<TextChannelCacheEntry>(data, guild)
    }

}

internal class TextChannelImpl(override val id: Snowflake, override val guild: Guild) : TextChannel {

    override fun toString(): String = "TextChannel(id=$id, type=$type)"
    override fun equals(other: Any?): Boolean = other is TextChannel && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

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
    override val name: String
) : TextChannel, GuildTextChannelCacheEntry {

    override val cacheManager = MessageCacheManager(client)

    override fun toString(): String = "TextChannelCacheEntry(id=$id, type=$type, name=$name)"
    override fun equals(other: Any?): Boolean = other is TextChannel && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}