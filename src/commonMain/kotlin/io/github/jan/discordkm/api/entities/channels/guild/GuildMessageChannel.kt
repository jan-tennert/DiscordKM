package io.github.jan.discordkm.api.entities.channels.guild

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.channels.MessageChannelCacheEntry
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.entities.channels.Invitable
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.restaction.buildRestAction

interface GuildMessageChannel : GuildChannel, MessageChannel {

    override val cache: GuildMessageChannelCacheEntry?

    /**
     * Removes the specified message from this channel
     * @param reason The reason which will be displayed in the audit log
     * @param messageIds The ids of the messages which are going to be removed
     */
    suspend fun removeMessages(messageIds: Iterable<Snowflake>, reason: String? = null) = client.buildRestAction<Unit> {
        route = Route.Message.BULK_DELETE(id).delete()
        this.reason = reason
    }

    companion object {
        operator fun invoke(id: Snowflake, guild: Guild) = guild.client.channels[id] as? GuildMessageChannelCacheEntry ?: GuildMessageChannelImpl(id, guild)
    }

}

internal class GuildMessageChannelImpl(override val id: Snowflake, override val guild: Guild) : GuildMessageChannel {

    override val cache: GuildMessageChannelCacheEntry?
        get() = guild.cache?.channels?.get(id) as? GuildMessageChannelCacheEntry
    override val type = cache?.type ?: ChannelType.UNKNOWN

    override fun toString(): String = "GuildMessageChannel(id=$id, type=$type)"
    override fun equals(other: Any?): Boolean = other is GuildMessageChannel && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}

sealed interface GuildMessageChannelCacheEntry : GuildMessageChannel, GuildChannelCacheEntry, MessageChannelCacheEntry, ParentChannel,
    Invitable {

    val slowModeTime: TimeSpan

}