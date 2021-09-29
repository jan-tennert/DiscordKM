package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.channels.GuildTextChannel
import io.github.jan.discordkm.api.entities.guild.channels.Thread

interface ThreadEvent : Event {

    val thread: Thread
    override val client: Client
        get() = thread.client

}

class ThreadCreateEvent(override val thread: Thread) : ThreadEvent
class ThreadUpdateEvent(override val thread: Thread, val oldThread: Thread?) : ThreadEvent
class ThreadDeleteEvent(
    override val client: Client,
    val threadId: Snowflake,
    val guildId: Snowflake,
    val guild: Guild,
    val parentId: Snowflake,
    val parent: GuildTextChannel,
    val threadMemberIds: List<Snowflake>,
    val threadMembers: List<Member>
) : Event
class ThreadMembersUpdateEvent(
    val threadId: Snowflake,
    override val thread: Thread,
    val memberCount: Int,
    val addedMembers: List<Thread.ThreadMember>,
    val removedMembers: List<Snowflake>
) : ThreadEvent
