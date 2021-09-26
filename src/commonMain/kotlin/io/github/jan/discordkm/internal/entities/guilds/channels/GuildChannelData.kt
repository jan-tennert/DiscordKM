package io.github.jan.discordkm.internal.entities.guilds.channels

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import io.github.jan.discordkm.Cache
import io.github.jan.discordkm.api.entities.PermissionHolder
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.api.entities.guild.channels.GuildTextChannel
import io.github.jan.discordkm.api.entities.guild.channels.PermissionOverride
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.api.entities.lists.MessageList
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.getEnums
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

open class GuildChannelData(override val guild: Guild, final override val data: JsonObject) : GuildChannel {

    override val permissionOverrides = data["permission_overwrites"]?.jsonArray?.map {
        val holder = when(it.jsonObject.getOrThrow<Int>("type")) {
            0 -> guild.roles[it.jsonObject.getOrThrow<Snowflake>("id")] as PermissionHolder
            1 -> guild.members[it.jsonObject.getOrThrow<Snowflake>("id")] as PermissionHolder
            else -> throw IllegalStateException()
        }
        val allow = it.jsonObject.getEnums("allow", Permission)
        val deny = it.jsonObject.getEnums("deny", Permission)
        PermissionOverride(holder, allow, deny)
    }?.toSet() ?: emptySet()

}



open class GuildTextChannelData(guild: Guild, data: JsonObject) : GuildChannelData(guild, data), GuildTextChannel, MessageChannel {

    val messageCache: Cache<Message> = Cache.fromSnowflakeEntityList(emptyList())
    override val messages: MessageList
        get() = MessageList(this, messageCache.values)

    override fun toString() = "GuildChannel[name=$name, id=$id, type=${ type}]"

    override suspend fun retrieveJoinedPrivateArchivedThreads(limit: Int?, before: Snowflake?) = client.buildRestAction<List<Thread>> {
        route = Route.Thread.GET_JOINED_PRIVATE_ARCHIVED_THREADS(id).get {
            putOptional("limit", limit)
            putOptional("before", before)
        }
        transform { it.toJsonObject().getValue("threads").jsonArray.map { thread -> ThreadData(guild, thread.jsonObject, it.toJsonObject().jsonArray.map { Json.decodeFromString("members") }) }}
        onFinish { it.forEach { thread -> (guild as GuildData).threadCache[thread.id] = thread } }
    }

    override suspend fun retrievePublicArchivedThreads(limit: Int?, before: DateTimeTz?) = client.buildRestAction<List<Thread>> {
        route = Route.Thread.GET_PUBLIC_ARCHIVED_THREADS(id).get {
            putOptional("limit", limit)
            putOptional("before", before?.let { ISO8601.DATETIME_UTC_COMPLETE.format(before) })
        }
        transform { it.toJsonObject().getValue("threads").jsonArray.map { thread -> ThreadData(guild, thread.jsonObject, it.toJsonObject().jsonArray.map { Json.decodeFromString("members") }) }}
        onFinish { it.forEach { thread -> (guild as GuildData).threadCache[thread.id] = thread } }
    }

    override suspend fun retrievePrivateArchivedThreads(limit: Int?, before: DateTimeTz?) = client.buildRestAction<List<Thread>> {
        route = Route.Thread.GET_PRIVATE_ARCHIVED_THREADS(id).get {
            putOptional("limit", limit)
            putOptional("before", before?.let { ISO8601.DATETIME_UTC_COMPLETE.format(before) })
        }
        transform { it.toJsonObject().getValue("threads").jsonArray.map { thread -> ThreadData(guild, thread.jsonObject, it.toJsonObject().jsonArray.map { Json.decodeFromString("members") }) }}
        onFinish { it.forEach { thread -> (guild as GuildData).threadCache[thread.id] = thread } }
    }

}