/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.guild.channels

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import com.soywiz.klock.minutes
import io.github.jan.discordkm.Cache
import io.github.jan.discordkm.entities.PermissionHolder
import io.github.jan.discordkm.entities.Reference
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.channels.Channel
import io.github.jan.discordkm.entities.channels.IParent
import io.github.jan.discordkm.entities.channels.Invitable
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.entities.guild.GuildEntity
import io.github.jan.discordkm.entities.guild.Permission
import io.github.jan.discordkm.entities.lists.ThreadList
import io.github.jan.discordkm.entities.messages.Message
import io.github.jan.discordkm.restaction.CallsTheAPI
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildQuery
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.getEnums
import io.github.jan.discordkm.utils.getId
import io.github.jan.discordkm.utils.getOrDefault
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.putJsonArray
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

sealed class GuildChannel (override val guild: Guild, final override val data: JsonObject) : Channel, Reference<GuildChannel>, GuildEntity {

    override fun getValue(ref: Any?, property: KProperty<*>): GuildChannel {
        TODO("Not yet implemented")
    }

    override val client = guild.client

    final override val id = data.getId()

    /**
     * The position of the channel in the hierarchy
     * Can be null if this guild channel is a [Thread]
     */
    val position = data.getOrNull<Int>("position")

    /**
     * The name of the channel
     */
    val name = data.getOrThrow<String>("name")


    /**
     * The [PermissionOverride]s for this guild channel
     */
    val permissionOverrides = data["permission_overwrites"]?.jsonArray?.map {
        val holder = when(it.jsonObject.getOrThrow<Int>("type")) {
            0 -> guild.roles[it.jsonObject.getOrThrow<Snowflake>("id")] as PermissionHolder
            1 -> guild.members[it.jsonObject.getOrThrow<Snowflake>("id")] as PermissionHolder
            else -> throw IllegalStateException()
        }
        val allow = it.jsonObject.getEnums("allow", Permission)
        val deny = it.jsonObject.getEnums("deny", Permission)
        PermissionOverride(holder, allow, deny)
    }?.toSet() ?: emptySet()

    /**
     * Deletes the channel.
     * Requires [Permission.MANAGE_CHANNELS] for Guild Channels and [Permission.MANAGE_THREADS] for Threads
     */
    @CallsTheAPI
    override suspend fun delete() = client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/channels/$id")
        transform {  }
        onFinish { guild.channelCache.remove(id) }
    }

    //permission overrides
}

sealed class GuildMessageChannel(guild: Guild, data: JsonObject) : GuildChannel(guild, data), MessageChannel {

    override var messageCache: Cache<Message> = Cache.fromSnowflakeEntityList(emptyList())
    /**
     * Deletes multiple messages in this channel
     */
    @CallsTheAPI
    suspend fun deleteMessages(ids: Iterable<Snowflake>) = client.buildRestAction<Unit> {
        action = RestAction.Action.post("/channels/$id/messages/bulk-delete", buildJsonObject {
            putJsonArray("messages") {
                ids.forEach { add(it.long) }
            }
        })
        transform {  }
        //TODO: Check permissions
    }

}
sealed class GuildTextChannel(guild: Guild, data: JsonObject) : GuildMessageChannel(guild, data), Invitable, IParent {

    /**
     * The topic of the channel
     */
    val topic = data.getOrNull<String>("topic")

    /**
     * The default time after threads get achieved in this channel
     */
    val defaultAutoArchiveDuration = if(data["default_auto_archive_duration"] != null) Thread.ThreadDuration.raw(data.getValue("default_auto_archive_duration").jsonPrimitive.int.minutes) else Thread.ThreadDuration.HOUR

    /**
     * Whether this channel is nsfw or not
     */
    @get:JvmName("isNSFW")
    val isNSFW = data.getOrDefault("nsfw", false)

    /**
     * Returns a list of all [Thread]s in this channel
     */
    val threads
        get() = ThreadList(guild.threads.filter { it.parentId == id })

    /**
     * Retrieves all public achieved threads
     * @param limit How many threads will get retrieved
     * @param before Threads before this timestamp
     */
    @CallsTheAPI
    suspend fun retrievePublicArchivedThreads(limit: Int? = null, before: DateTimeTz? = null) = client.buildRestAction<List<Thread>> {
        action = RestAction.Action.get("/channels/${id}/threads/archived/public" + buildQuery {
            putOptional("limit", limit)
            putOptional("before", before?.let { ISO8601.DATETIME_UTC_COMPLETE.format(before) })
        })
        transform { it.toJsonObject().getValue("threads").jsonArray.map { thread -> Thread(guild, thread.jsonObject, it.toJsonObject().jsonArray.map { Json.decodeFromString("members") }) }}
        onFinish { it.forEach { thread -> guild.threadCache[thread.id] = thread } }
    }

    /**
     * Retrieves all private achieved threads
     * @param limit How many threads will get retrieved
     * @param before Threads before this timestamp
     */
    @CallsTheAPI
    suspend fun retrievePrivateArchivedThreads(limit: Int? = null, before: DateTimeTz? = null) = client.buildRestAction<List<Thread>> {
        action = RestAction.Action.get("/channels/${id}/threads/archived/private" + buildQuery {
            putOptional("limit", limit)
            putOptional("before", before?.let { ISO8601.DATETIME_UTC_COMPLETE.format(before) })
        })
        transform { it.toJsonObject().getValue("threads").jsonArray.map { thread -> Thread(guild, thread.jsonObject, it.toJsonObject().jsonArray.map { Json.decodeFromString("members") }) }}
        onFinish { it.forEach { thread -> guild.threadCache[thread.id] = thread } }
    }

    /**
     * Retrieves all joined private archived threads
     * @param limit How many threads will get retrieved
     * @param before Threads before this id
     */
    @CallsTheAPI
    suspend fun retrieveJoinedPrivateArchivedThreads(limit: Int? = null, before: Snowflake? = null) = client.buildRestAction<List<Thread>> {
        action = RestAction.Action.get("/channels/${id}/users/@me/threads/archived/private" + buildQuery {
            putOptional("limit", limit)
            putOptional("before", before)
        })
        transform { it.toJsonObject().getValue("threads").jsonArray.map { thread -> Thread(guild, thread.jsonObject, it.toJsonObject().jsonArray.map { Json.decodeFromString("members") }) }}
        onFinish { it.forEach { thread -> guild.threadCache[thread.id] = thread } }
    }

}