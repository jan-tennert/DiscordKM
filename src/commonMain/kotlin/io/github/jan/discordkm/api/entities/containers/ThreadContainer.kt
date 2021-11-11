package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.channels.guild.ThreadCacheEntry
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.entities.guilds.channels.ThreadData
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.put
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.toJsonArray
import kotlinx.serialization.json.jsonObject

class ThreadContainer(override val values: Collection<ThreadCacheEntry>) : NameableSnowflakeContainer<ThreadCacheEntry>

class ThreadMemberContainer(val thread: Thread) {

    /**
     * Retrieves all [ThreadMembers] from this [thread]
     */
    suspend fun retrieveMembers() = thread.client.buildRestAction<List<Thread.ThreadMember>> {
        route = Route.Thread.GET_THREAD_MEMBERS(thread.id).get()
        transform { it.toJsonArray().map { t -> Thread.ThreadMember.from(t.jsonObject, thread.guild) } }
    }

    /**
     * Adds a member to this thread
     */
    suspend fun add(member: Member) = thread.client.buildRestAction<Unit> {
        route = Route.Thread.ADD_THREAD_MEMBER(thread.id, member.id).put()
    }

    /**
     * Removes a member from this thread
     */
    suspend fun remove(member: Member) = thread.client.buildRestAction<Unit> {
        route = Route.Thread.REMOVE_THREAD_MEMBER(thread.id, member.id).delete()
    }

    suspend operator fun plusAssign(member: Member) = add(member)

    suspend operator fun minusAssign(member: Member) = remove(member)
}