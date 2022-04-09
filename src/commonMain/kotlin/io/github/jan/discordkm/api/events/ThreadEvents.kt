/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.guild.GuildTextChannel
import io.github.jan.discordkm.api.entities.channels.guild.StageChannel
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.channels.guild.ThreadCacheEntry
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.message.DataMessage
import io.github.jan.discordkm.api.entities.message.MessageCacheEntry

interface ThreadEvent : Event {

    val thread: Thread
    override val client: DiscordClient
        get() = thread.client

}

/*
 * Sent when a thread was created
 */
class ThreadCreateEvent(override val thread: ThreadCacheEntry) : ThreadEvent, ThreadCacheEntry by thread {

    override val client = thread.client

    override suspend fun send(message: DataMessage): MessageCacheEntry {
        return thread.send(message)
    }

}

/*
 * Sent when a thread was updated
 */
class ThreadUpdateEvent(override val thread: ThreadCacheEntry, val oldThread: ThreadCacheEntry?) : ThreadEvent, ThreadCacheEntry by thread {

    override val client = thread.client

    override suspend fun send(message: DataMessage): MessageCacheEntry {
        return thread.send(message)
    }

}

/*
 * Sent when a thread was deleted
 */
class ThreadDeleteEvent(
    override val client: DiscordClient,
    val threadId: Snowflake,
    val guild: Guild,
    val stageChannel: StageChannel,
    val type: ChannelType
) : Event

class ThreadMembersUpdateEvent(
    override val thread: Thread,
    val memberCount: Int,
    val addedMembers: List<Thread.ThreadMember>,
    val removedMembers: List<Snowflake>
) : ThreadEvent

class ThreadListSyncEvent(
    val guild: Guild,
    val threads: List<ThreadCacheEntry>,
    val threadMembers: List<Thread.ThreadMember>,
    val channels: List<GuildTextChannel>
) : Event {

    override val client: DiscordClient
        get() = guild.client

}
