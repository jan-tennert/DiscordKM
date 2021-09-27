package io.github.jan.discordkm.api.entities.lists

import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.entities.guilds.channels.ThreadData
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.put
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.toJsonObject

class ThreadMemberList(private val thread: Thread, override val internalList: List<Thread.ThreadMember>) : DiscordList<Thread.ThreadMember> {

    override fun get(name: String) = internalList.filter { it.user.name == name }

    /**
     * Retrieves all [ThreadMembers] from this [thread]
     */
    suspend fun retrieveMembers() = thread.client.buildRestAction<List<Thread.ThreadMember>> {
        route = Route.Thread.GET_THREAD_MEMBERS(thread.id).get()
        transform { it.toJsonObject().extractGuildEntity(thread.guild) }
        onFinish { (thread as ThreadData).memberCache.internalMap.clear(); thread.memberCache.internalMap.putAll( it.associateBy { member -> member.id }) }
    }

    /**
     * Adds a member to this thread
     */
    suspend fun add(member: Member) = thread.client.buildRestAction<Unit> {
        route = Route.Thread.ADD_THREAD_MEMBER(thread.id, member.id).put()
        transform { }
    }


    /**
     * Removes a member from this thread
     */
    suspend fun remove(member: Member) = thread.client.buildRestAction<Unit> {
        route = Route.Thread.REMOVE_THREAD_MEMBER(thread.id, member.id).delete()
        transform { }
    }


    suspend operator fun plusAssign(member: Member) = add(member)


    suspend operator fun minusAssign(member: Member) = remove(member)


}

class ThreadList(override val internalList: List<Thread>) : DiscordList<Thread> {

    override fun get(name: String) = internalList.filter { it.name == name }

}