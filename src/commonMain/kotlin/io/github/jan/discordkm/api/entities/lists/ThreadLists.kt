package io.github.jan.discordkm.api.entities.lists

import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.internal.entities.guilds.channels.ThreadData

import io.github.jan.discordkm.internal.restaction.RestAction
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.toJsonObject

class ThreadMemberList(private val thread: Thread, override val internalList: List<Thread.ThreadMember>) : DiscordList<Thread.ThreadMember> {

    override fun get(name: String) = internalList.filter { it.user.name == name }


    suspend fun retrieveMembers() = thread.client.buildRestAction<List<Thread.ThreadMember>> {
        action = RestAction.get("/channels/${thread.id}/thread-members")
        transform { it.toJsonObject().extractGuildEntity(thread.guild) }
        onFinish { (thread as ThreadData).memberCache.internalMap.clear(); thread.memberCache.internalMap.putAll( it.associateBy { member -> member.id }) }
    }


    suspend fun add(member: Member) = thread.client.buildRestAction<Unit> {
        action = RestAction.put("/channels/${thread.id}/thread-members/${member.id}")
        transform { }
    }


    suspend fun remove(member: Member) = thread.client.buildRestAction<Unit> {
        action = RestAction.delete("/channels/${thread.id}/thread-members/${member.id}")
        transform { }
    }


    suspend operator fun plusAssign(member: Member) = add(member)


    suspend operator fun minusAssign(member: Member) = remove(member)


}

class ThreadList(override val internalList: List<Thread>) : DiscordList<Thread> {

    override fun get(name: String) = internalList.filter { it.name == name }

}