package io.github.jan.discordkm.api.entities.channels.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.entities.message.MessageBuilder
import io.github.jan.discordkm.api.entities.modifier.Modifiable
import io.github.jan.discordkm.api.entities.modifier.guild.ForumChannelModifier
import io.github.jan.discordkm.api.entities.modifier.guild.GuildChannelBuilder
import io.github.jan.discordkm.api.entities.modifier.guild.TextChannelModifier
import io.github.jan.discordkm.internal.DiscordKMUnstable
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.toJsonObject

@DiscordKMUnstable
sealed interface ForumChannel : GuildChannel, InvitableGuildChannel, Modifiable<ForumChannelModifier, ForumChannel>, ThreadParent {

    override val type: ChannelType
        get() = ChannelType.GUILD_FORUM
    override val cache: ForumChannelCacheEntry?
        get() = guild.cache?.cacheManager?.channelCache?.get(id) as? ForumChannelCacheEntry

    override suspend fun modify(reason: String?, modifier: ForumChannelModifier.() -> Unit): ForumChannel = client.buildRestAction<ForumChannelCacheEntry> {
        route = Route.Channel.MODIFY_CHANNEL(id).patch(ForumChannelModifier().apply(modifier).data)
        this.reason = reason
        transform { ChannelSerializer.deserialize(it.toJsonObject(), guild) as ForumChannelCacheEntry }
    }

    /**
     * Creates a new thread in this forum channel
     */
    suspend fun createThread(builder: ForumChannelThreadBuilder.() -> Unit): ThreadCacheEntry
    /**
     * Creates a new thread in this forum channel
     * @param name the name of the thread
     * @param autoArchiveDuration the duration after the thread will be achieved
     * @param slowModeTime the timeout for sending messages
     * @param message The first message to be sent in the thread
     */
    suspend fun createThread(name: String, autoArchiveDuration: Thread.ThreadDuration? = null, slowModeTime: TimeSpan? = null, message: MessageBuilder.() -> Unit): ThreadCacheEntry

    companion object : GuildChannelBuilder<ForumChannelModifier, ForumChannel> {

        override fun createChannel(modifier: ForumChannelModifier.() -> Unit) = ForumChannelModifier().apply(modifier)

    }

}