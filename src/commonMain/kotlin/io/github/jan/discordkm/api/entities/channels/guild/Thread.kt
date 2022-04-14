/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
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
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.entities.message.Message
import io.github.jan.discordkm.api.entities.modifier.Modifiable
import io.github.jan.discordkm.api.entities.modifier.guild.ThreadModifier
import io.github.jan.discordkm.internal.Route
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

sealed interface Thread : GuildMessageChannel, Modifiable<ThreadModifier, ThreadCacheEntry> {

    val members: ThreadMemberContainer
        get() = ThreadMemberContainer(this)
    val firstMessage: Message
        get() = Message(id, this)
    override val cache: ThreadCacheEntry?
        get() = guild.cache?.cacheManager?.channelCache?.get(id) as? ThreadCacheEntry

    /*
     * Joins this thread.
     */
    suspend fun join() = client.buildRestAction<Unit> {
        route = Route.Thread.JOIN_THREAD(id).put()
    }

    /*
     * Leaves this thread.
     */
    suspend fun leave() = client.buildRestAction<Unit> {
        route = Route.Thread.LEAVE_THREAD(id).delete()
    }

    override suspend fun modify(reason: String?, modifier: ThreadModifier.() -> Unit) = client.buildRestAction<ThreadCacheEntry> {
        route = Route.Channel.MODIFY_CHANNEL(id).patch(ThreadModifier().apply(modifier).data)
        this.reason = reason
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
        @Serializable(with = ISO8601Serializer::class)
        @SerialName("create_timestamp")
        val creationDate: DateTimeTz,
        @SerialName("archived")
        val isArchived: Boolean,
        @SerialName("auto_archive_duration")
        val autoArchiveDuration: ThreadDuration,
        @SerialName("archive_timestamp")
        @Serializable(with = ISO8601Serializer::class)
        val archiveDate: DateTimeTz,
        @SerialName("locked")
        val isLocked: Boolean,
        @SerialName("invitable")
        val isInvitable: Boolean? = null
    )

    companion object {
        operator fun invoke(id: Snowflake, guild: Guild, type: ChannelType): Thread = ThreadImpl(id, guild, type)

        operator fun invoke(data: JsonObject, guild: Guild) = ChannelSerializer.deserializeChannel<ThreadCacheEntry>(data, guild)
    }

    /*
     * Represents the time when a thread is getting archived
     */
    @Serializable(with = ThreadDurationSerializer::class)
    @JvmInline
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

internal class ThreadImpl(override val id: Snowflake, override val guild: Guild, override val type: ChannelType) : Thread {

    override fun toString(): String = "Thread(id=$id, type=$type)"
    override fun equals(other: Any?): Boolean = other is Thread && other.id == id && other.guild.id == guild.id
    override fun hashCode(): Int = id.hashCode()

}