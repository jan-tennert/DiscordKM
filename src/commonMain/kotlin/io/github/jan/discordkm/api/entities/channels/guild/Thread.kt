package io.github.jan.discordkm.api.entities.channels.guild

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.soywiz.klock.weeks
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.containers.ThreadMemberContainer
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.PermissionOverwrite
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.entities.modifiers.Modifiable
import io.github.jan.discordkm.api.entities.modifiers.guild.TextChannelModifier
import io.github.jan.discordkm.api.entities.modifiers.guild.ThreadModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.MessageCacheManager
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.put
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.ISO8601Serializer
import io.github.jan.discordkm.internal.utils.ThreadDurationSerializer
import io.github.jan.discordkm.internal.utils.isoTimestamp
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmInline

interface Thread : GuildMessageChannel, Modifiable<ThreadModifier, ThreadCacheEntry> {

    val members: ThreadMemberContainer
        get() = ThreadMemberContainer(this)
    override val cache: ThreadCacheEntry?
        get() = guild.cache?.cacheManager?.channelCache?.get(id) as? ThreadCacheEntry

    /**
     * Joins this thread.
     */
    suspend fun join() = client.buildRestAction<Unit> {
        route = Route.Thread.JOIN_THREAD(id).put()
    }

    /**
     * Leaves this thread.
     */
    suspend fun leave() = client.buildRestAction<Unit> {
        route = Route.Thread.LEAVE_THREAD(id).delete()
    }

    override suspend fun modify(modifier: ThreadModifier.() -> Unit) = client.buildRestAction<ThreadCacheEntry> {
        route = Route.Channel.MODIFY_CHANNEL(id).patch(ThreadModifier().apply(modifier).data)
        transform { ChannelSerializer.deserializeChannel(it.toJsonObject(), guild) }
    }

    data class ThreadMember(
        val guild: Guild,
        val user: User,
        val thread: Thread,
        val joinedAt: DateTimeTz,
        override val id: Snowflake = user.id
    )  : SnowflakeEntity {

        companion object {
            operator fun invoke(data: JsonObject, guild: Guild) = ThreadMember(
                guild = guild,
                user = User(data["user_id"]!!.snowflake, guild.client),
                thread = invoke(data["id"]!!.snowflake, guild, ChannelType.GUILD_PUBLIC_THREAD),
                joinedAt = data["joined_at"]!!.isoTimestamp
            )
        }

    }


    @Serializable
    data class ThreadMetadata(
        @SerialName("archived")
        val isArchived: Boolean,
        @SerialName("auto_archive_duration")
        val autoArchiveDuration: Thread.ThreadDuration,
        @SerialName("archive_timestamp")
        @Serializable(with = ISO8601Serializer::class)
        val archiveTimestamp: DateTimeTz,
        @SerialName("locked")
        val isLocked: Boolean,
        @SerialName("invitable")
        val isInvitable: Boolean? = null
    )

    companion object {
        operator fun invoke(id: Snowflake, guild: Guild, type: ChannelType) = guild.client.threads[id] ?: object : Thread {
            override val guild = guild
            override val id = id
            override val type = guild.cache?.threads?.get(id)?.type ?: type
        }

        operator fun invoke(data: JsonObject, guild: Guild) = ChannelSerializer.deserializeChannel<ThreadCacheEntry>(data, guild)
    }

    /**
     * Represents the time when a thread is getting archived
     */
    @JvmInline
    @Serializable(with = ThreadDurationSerializer::class)
    value class ThreadDuration private constructor(val duration: TimeSpan) {

        companion object {
            val HOUR = ThreadDuration(1.hours)
            val DAY = ThreadDuration(1.days)
            val THREE_DAYS = ThreadDuration(3.days)
            val WEEK = ThreadDuration(1.weeks)

            @PublishedApi
            internal fun raw(duration: TimeSpan) = ThreadDuration(duration)
        }

    }

}

class ThreadCacheEntry(
    override val guild: Guild,
    override val permissionOverwrites: Set<PermissionOverwrite>,
    override val slowModeTime: TimeSpan,
    override val parent: GuildTextChannel,
    override val id: Snowflake,
    override val lastMessage: Message?,
    override val name: String,
    override val type: ChannelType,
    val metadata: Thread.ThreadMetadata,
) : Thread, GuildMessageChannelCacheEntry {

    override val cacheManager = MessageCacheManager(client)

}