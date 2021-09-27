/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.guild.channels

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.soywiz.klock.parse
import com.soywiz.klock.weeks
import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.channels.modifier.ThreadModifier
import io.github.jan.discordkm.api.entities.lists.ThreadMemberList
import io.github.jan.discordkm.api.entities.messages.DataMessage
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.internal.entities.channels.IParent
import io.github.jan.discordkm.internal.utils.ISO8601Serializer
import io.github.jan.discordkm.internal.utils.ThreadDurationSerializer
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmInline

//maybe cache threads?
interface Thread : GuildTextChannel, IParent {

    override val parent
        get() = guild.channels[parentId ?: Snowflake.empty()] as? GuildTextChannel

    val metadata: ThreadMetadata
        get() = Json.decodeFromString<ThreadMetadata>(data.getOrThrow<String>("thread_metadata"))

    val members: ThreadMemberList

    /**
     * Joins this [Thread]
     */

    suspend fun join()

    /**
     * Retrieves all thread members from this thread
     *
     * Requires the intent [Intent.GUILD_MEMBERS]
     */
    suspend fun retrieveThreadMembers(): List<ThreadMember>

    /**
     * Leaves this thread
     */

    suspend fun leave()


    override suspend fun send(message: DataMessage): Message

    /**
     * Modifies this [Thread]
     */

    suspend fun modify(modifier: ThreadModifier.() -> Unit): Thread

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

        override val id = data.getOrThrow<Snowflake>("user_id")

        val threadId = data.getId()

        override val client = guild.client

        val user: User
            get() = client.users[id]!!

        /**
         * The date when the member joined this thead.
         */
        val joinedAt = ISO8601.DATETIME_UTC_COMPLETE.parse(data.getOrThrow<String>("join_timestamp"))

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