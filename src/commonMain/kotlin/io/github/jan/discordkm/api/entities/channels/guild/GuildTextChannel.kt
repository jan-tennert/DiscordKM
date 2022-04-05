package io.github.jan.discordkm.api.entities.channels.guild

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.containers.CacheGuildThreadContainer
import io.github.jan.discordkm.api.entities.containers.GuildThreadContainer
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.entities.modifiers.Modifiable
import io.github.jan.discordkm.api.entities.modifiers.guild.TextChannelModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put


interface GuildTextChannel : GuildMessageChannel, Modifiable<TextChannelModifier, GuildTextChannelCacheEntry>, InvitableGuildChannel {

    /**
     * Creates a new public thread in this channel.
     * @param name The name of the thread.
     * @param autoArchiveDuration The time after which the thread will be automatically archived.
     * @param slowModeTime The time after users will be able to send another message
     * @param reason The reason which will be displayed in the audit logs
     * @param invitable Whether the thread can be joined by anyone
     */
    suspend fun createPublicThread(name: String, autoArchiveDuration: Thread.ThreadDuration = Thread.ThreadDuration.DAY, slowModeTime: TimeSpan? = null, reason: String? = null, invitable: Boolean? = null) = createThread(name, ChannelType.GUILD_PUBLIC_THREAD, autoArchiveDuration, slowModeTime, reason, invitable)

    /**
     * Creates a new private thread in this channel.
     * @param name The name of the thread.
     * @param autoArchiveDuration The time after which the thread will be automatically archived.
     * @param slowModeTime The time after users will be able to send another message
     * @param reason The reason which will be displayed in the audit logs
     * @param invitable Whether the thread can be joined by anyone
     */
    suspend fun createPrivateThread(name: String, autoArchiveDuration: Thread.ThreadDuration = Thread.ThreadDuration.DAY, slowModeTime: TimeSpan? = null, reason: String? = null, invitable: Boolean? = null) = createThread(name, ChannelType.GUILD_PRIVATE_THREAD, autoArchiveDuration, slowModeTime, reason, invitable)

    suspend fun createThread(name: String, type: ChannelType, autoArchiveDuration: Thread.ThreadDuration = Thread.ThreadDuration.DAY, slowModeTime: TimeSpan? = null, reason: String? = null, invitable: Boolean? = null) = client.buildRestAction<Unit> {
        transform { Thread(it.toJsonObject(), guild) }
        route = Route.Thread.START_THREAD(id).post(buildJsonObject {
            put("name", name)
            putOptional("rate_limit_per_user", slowModeTime?.seconds)
            put("auto_archive_duration", autoArchiveDuration.duration.minutes.toInt())
            put("invitable", invitable)
        })
        this.reason = reason
    }

    /**
     * Retrieves all public achieved threads
     * @param limit How many threads will get retrieved
     * @param before Threads before this timestamp
     */
    suspend fun retrieveJoinedPrivateArchivedThreads(limit: Int?, before: Snowflake?) = client.buildRestAction<List<Thread>> {
        route = Route.Thread.GET_JOINED_PRIVATE_ARCHIVED_THREADS(id).get {
            putOptional("limit", limit)
            putOptional("before", before)
        }
        transform { it.toJsonObject().getValue("threads").jsonArray.map { thread -> Thread(thread.jsonObject, guild) }}
    }

    /**
     * Retrieves all private achieved threads
     * @param limit How many threads will get retrieved
     * @param before Threads before this timestamp
     */
    suspend fun retrievePublicArchivedThreads(limit: Int?, before: DateTimeTz?) = client.buildRestAction<List<Thread>> {
        route = Route.Thread.GET_PUBLIC_ARCHIVED_THREADS(id).get {
            putOptional("limit", limit)
            putOptional("before", before?.let { ISO8601.DATETIME_UTC_COMPLETE.format(before) })
        }
        transform { it.toJsonObject().getValue("threads").jsonArray.map { thread -> Thread(thread.jsonObject, guild) }}
    }

    /**
     * Retrieves all joined private archived threads
     * @param limit How many threads will get retrieved
     * @param before Threads before this id
     */
    suspend fun retrievePrivateArchivedThreads(limit: Int?, before: DateTimeTz?) = client.buildRestAction<List<Thread>> {
        route = Route.Thread.GET_PRIVATE_ARCHIVED_THREADS(id).get {
            putOptional("limit", limit)
            putOptional("before", before?.let { ISO8601.DATETIME_UTC_COMPLETE.format(before) })
        }
        transform { it.toJsonObject().getValue("threads").jsonArray.map { thread -> Thread(thread.jsonObject, guild) }}
    }

    override suspend fun modify(reason: String?, modifier: TextChannelModifier.() -> Unit) = client.buildRestAction<GuildTextChannelCacheEntry> {
        route = Route.Channel.MODIFY_CHANNEL(id).patch(TextChannelModifier().apply(modifier).data)
        this.reason = reason
        transform { ChannelSerializer.deserialize(it.toJsonObject(), guild) as GuildTextChannelCacheEntry }
    }

    companion object {
        operator fun invoke(id: Snowflake, guild: Guild) = guild.client.channels[id] as? GuildTextChannelCacheEntry ?: object : GuildTextChannel {
            override val cache: GuildMessageChannelCacheEntry?
                get() = guild.cache?.channels?.get(id) as GuildMessageChannelCacheEntry?
            override val guild: Guild = guild
            override val type: ChannelType
                get() = cache?.type ?: ChannelType.UNKNOWN
            override val id: Snowflake = id
        }
    }

}

sealed interface GuildTextChannelCacheEntry : GuildTextChannel, GuildMessageChannelCacheEntry, IPositionable {

    val threads: GuildThreadContainer
        get() = guild.cache?.cacheManager?.threadCache?.filter { it.value.parent.id == id }?.values?.let {
            CacheGuildThreadContainer(
                guild,
                it
            )
        } ?: CacheGuildThreadContainer(guild, emptyList())
    val isNSFW: Boolean
    val topic: String?
    val defaultAutoArchiveDuration: Thread.ThreadDuration

}