package io.github.jan.discordkm.api.entities.channels.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.entities.modifiers.guild.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.modifiers.guild.TextChannelModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.MessageCacheManager
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface NewsChannel: GuildTextChannel {

    override val type: ChannelType
        get() = ChannelType.GUILD_NEWS
    override val cache: NewsChannelCacheEntry?
        get() = guild.cache?.cacheManager?.channelCache?.get(id) as? NewsChannelCacheEntry

    /**
     * Follows the news channel which means messages which are sent in this channel can be published to send the message to every channel which is following this channel.
     * @param targetId The id of the channel where the messages are going to be sent when a new message is published
     */
    suspend fun follow(targetId: Snowflake) = client.buildRestAction<Unit> {
        route = Route.Channel.FOLLOW_CHANNEL(id).post(buildJsonObject {
            put("webhook_channel_id", targetId.long)
        })
    }

    suspend fun follow(target: GuildTextChannel) = follow(target.id)

    companion object : GuildChannelBuilder<TextChannelModifier, NewsChannel> {
        override fun create(modifier: TextChannelModifier.() -> Unit) = TextChannelModifier().apply { convertToNewsChannel(); modifier(this) }

        operator fun invoke(id: Snowflake, guild: Guild): NewsChannel = IndependentNewsChannel(id, guild)
        operator fun invoke(data: JsonObject, guild: Guild) = ChannelSerializer.deserializeChannel<NewsChannelCacheEntry>(data, guild)
    }

}

data class IndependentNewsChannel(override val id: Snowflake, override val guild: Guild) : NewsChannel

class NewsChannelCacheEntry(
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
) : NewsChannel, GuildTextChannelCacheEntry {

    override val cacheManager = MessageCacheManager(client)

}