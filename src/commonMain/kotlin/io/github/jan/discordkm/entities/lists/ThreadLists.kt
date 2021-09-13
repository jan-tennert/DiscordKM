package io.github.jan.discordkm.entities.lists

import io.github.jan.discordkm.entities.guild.Member
import io.github.jan.discordkm.entities.guild.channels.Thread
import io.github.jan.discordkm.restaction.CallsTheAPI
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.toJsonObject

class ThreadMemberList(private val thread: Thread, override val internalList: List<Thread.ThreadMember>) : DiscordList<Thread.ThreadMember> {

    override fun get(name: String) = internalList.filter { it.user.name == name }

    @CallsTheAPI
    suspend fun retrieveMembers() = thread.client.buildRestAction<List<Thread.ThreadMember>> {
        action = RestAction.Action.get("/channels/${thread.id}/thread-members")
        transform { it.toJsonObject().extractGuildEntity(thread.guild) }
        onFinish { thread.memberCache.internalMap.clear(); thread.memberCache.internalMap.putAll( it.associateBy { member -> member.id }) }
    }

    @CallsTheAPI
    suspend fun add(member: Member) = thread.client.buildRestAction<Unit> {
        action = RestAction.Action.put("/channels/${thread.id}/thread-members/${member.id}")
        transform { }
    }

    @CallsTheAPI
    suspend fun remove(member: Member) = thread.client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/channels/${thread.id}/thread-members/${member.id}")
        transform { }
    }

    @CallsTheAPI
    suspend operator fun plusAssign(member: Member) = add(member)

    @CallsTheAPI
    suspend operator fun minusAssign(member: Member) = remove(member)


}

class ThreadList(override val internalList: List<Thread>) : DiscordList<Thread> {

    override fun get(name: String) = internalList.filter { it.name == name }

}