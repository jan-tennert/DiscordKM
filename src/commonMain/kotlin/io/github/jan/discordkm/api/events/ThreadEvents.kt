/**
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
import io.github.jan.discordkm.api.entities.channels.guild.StageChannel
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild

interface ThreadEvent : Event {

    val thread: Thread
    override val client: Client
        get() = thread.client

}

/**
 * Sent when a thread was created
 */
class ThreadCreateEvent(override val thread: Thread) : ThreadEvent

/**
 * Sent when a thread was updated
 */
class ThreadUpdateEvent(override val thread: Thread, val oldThread: Thread?) : ThreadEvent

/**
 * Sent when a thread was deleted
 */
class ThreadDeleteEvent(
    override val client: Client,
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
