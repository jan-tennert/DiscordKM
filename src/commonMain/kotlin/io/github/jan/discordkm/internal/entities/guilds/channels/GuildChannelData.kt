/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.entities.guilds.channels

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import io.github.jan.discordkm.api.entities.PermissionHolder
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.api.entities.guild.channels.GuildTextChannel
import io.github.jan.discordkm.api.entities.guild.channels.PermissionOverride
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.internal.EntityCache
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

    override val messageCache: EntityCache<Snowflake, Message> = EntityCache.fromSnowflakeEntityList(emptyList())

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