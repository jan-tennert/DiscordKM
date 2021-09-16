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
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.soywiz.klock.weeks
import io.github.jan.discordkm.Cache
import io.github.jan.discordkm.entities.SerializableEntity
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.SnowflakeEntity
import io.github.jan.discordkm.entities.channels.IParent
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.entities.guild.channels.modifier.ThreadModifier
import io.github.jan.discordkm.entities.lists.ThreadMemberList
import io.github.jan.discordkm.entities.messages.DataMessage
import io.github.jan.discordkm.entities.messages.Message
import io.github.jan.discordkm.restaction.CallsTheAPI
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.ISO8601Serializer
import io.github.jan.discordkm.utils.ThreadDurationSerializer
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.extractMessageChannelEntity
import io.github.jan.discordkm.utils.getId
import io.github.jan.discordkm.utils.getOrThrow
import io.github.jan.discordkm.utils.toJsonArray
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.jvm.JvmInline

//maybe cache threads?
class Thread(guild: Guild, data: JsonObject, members: List<ThreadMember> = emptyList()) : GuildTextChannel(guild, data), IParent {

    internal var memberCache = Cache.fromSnowflakeEntityList<ThreadMember>(members)

    override val parent
        get() = guild.channels[parentId ?: Snowflake.empty()] as? GuildTextChannel

    val metadata = Json.decodeFromString<ThreadMetadata>(data.getOrThrow<String>("thread_metadata"))

    val members
        get() = ThreadMemberList(this, memberCache.values)

    /**
     * Joins this [Thread]
     */
    @CallsTheAPI
    suspend fun join() = client.buildRestAction<Unit> {
        action = RestAction.Action.put("/channels/$id/thread-members/@me")
        transform {  }
        check { if(metadata.isArchived) throw UnsupportedOperationException("This thread is archived. You can't join anymore ") }
    }

    /**
     * Retrieves all thread members from this thread
     *
     * Requires the intent [Intent.GUILD_MEMBERS]
     */
    suspend fun retrieveThreadMembers() = client.buildRestAction<List<ThreadMember>> {
        action = RestAction.Action.get("/channels/$id/thread-members")
        transform { it.toJsonArray().map { json -> ThreadMember(guild, json.jsonObject) }}
        onFinish { memberCache.internalMap.clear(); memberCache.internalMap.putAll(it.associateBy { member -> member.id }) }
    }

    /**
     * Leaves this thread
     */
    @CallsTheAPI
    suspend fun leave() = client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/channels/$id/thread-members/@me")
        transform {  }
    }

    @CallsTheAPI
    override suspend fun send(message: DataMessage) = client.buildRestAction<Message> {
        action = RestAction.Action.post("/channels/$id/messages", Json.encodeToString(message))
        transform {
            it.toJsonObject().extractMessageChannelEntity(this@Thread)
        }
        check { if(metadata.isArchived) throw UnsupportedOperationException("This thread is archived. You can send messages in it anymore ")}
    }

    /**
     * Modifies this [Thread]
     */
    @CallsTheAPI
    suspend fun modify(modifier: ThreadModifier.() -> Unit) = client.buildRestAction<Thread> {
        action = RestAction.Action.patch("/channels/${id}", ThreadModifier(this@Thread).apply(modifier).build())
        transform { it.toJsonObject().extractGuildEntity(guild) }
        onFinish { guild.threadCache[id] = it }
    }

    override suspend fun retrieve() = throw UnsupportedOperationException()


    @Serializable
    data class ThreadMetadata(
        @SerialName("archived")
        val isArchived: Boolean,
        @SerialName("auto_archive_duration")
        val autoArchiveDuration: ThreadDuration,
        @SerialName("archive_timestamp")
        @Serializable(with = ISO8601Serializer::class)
        val archiveTimestamp: DateTimeTz,
        @SerialName("locked")
        val isLocked: Boolean,
        @SerialName("invitable")
        val isInvitable: Boolean? = null
    )

    data class ThreadMember(val guild: Guild, override val data: JsonObject) : SerializableEntity, SnowflakeEntity {

        override val id = data.getId()
        override val client = guild.client

        val user = client.users[id]!!

        val joinedAt = Json.decodeFromString(ISO8601Serializer, data.getOrThrow("join_timestamp"))

    }

    /**
     * Represents the time when a thread is getting archived
     */
    @JvmInline
    @Serializable(with = ThreadDurationSerializer::class)
    value class ThreadDuration internal constructor(val duration: TimeSpan) {

        companion object {
            val HOUR = ThreadDuration(1.hours)
            val DAY = ThreadDuration(1.days)
            val THREE_DAYS = ThreadDuration(3.days)
            val WEEK = ThreadDuration(1.weeks)

            internal fun raw(duration: TimeSpan) = ThreadDuration(duration)
        }

    }

}