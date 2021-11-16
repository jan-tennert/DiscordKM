package io.github.jan.discordkm.api.entities.channels.guild

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.containers.CacheGuildThreadContainer
import io.github.jan.discordkm.api.entities.containers.GuildThreadContainer
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

interface GuildTextChannel : GuildMessageChannel {

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

    companion object {
        operator fun invoke(id: Snowflake, guild: Guild) = guild.client.channels[id] ?: object : GuildTextChannel {
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