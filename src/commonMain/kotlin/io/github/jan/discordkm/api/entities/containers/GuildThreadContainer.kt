/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.channels.guild.ThreadCacheEntry
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.member.Member
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.put
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.channel.ChannelSerializer
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

open class GuildThreadContainer(val guild: Guild) {

    /*
     * Retrieves all active threads in the guild.
     */
    suspend fun retrieveActiveThreads() = guild.client.buildRestAction<List<Thread>> {
        route = Route.Thread.GET_ACTIVE_THREADS(guild.id).get()
        transform {
            it.toJsonObject()["threads"]!!.jsonArray.map { thread ->
                ChannelSerializer.deserializeChannel<ThreadCacheEntry>(thread.jsonObject, guild)
            }
        }
    }

}

class CacheThreadContainer(override val values: Collection<ThreadCacheEntry>) : NameableSnowflakeContainer<ThreadCacheEntry>
class CacheGuildThreadContainer(guild: Guild, override val values: Collection<ThreadCacheEntry>) : NameableSnowflakeContainer<ThreadCacheEntry>, GuildThreadContainer(guild)

class ThreadMemberContainer(val thread: Thread) {

    /*
     * Retrieves all [ThreadMembers] from this [thread]
     */
    suspend fun retrieveMembers() = thread.client.buildRestAction<List<Thread.ThreadMember>> {
        route = Route.Thread.GET_THREAD_MEMBERS(thread.id).get()
        transform { it.toJsonArray().map { t -> Thread.ThreadMember(t.jsonObject, thread.guild) } }
    }

    /*
     * Adds a member to this thread
     */
    suspend fun add(member: Member) = thread.client.buildRestAction<Unit> {
        route = Route.Thread.ADD_THREAD_MEMBER(thread.id, member.id).put()
    }

    /*
     * Removes a member from this thread
     */
    suspend fun remove(member: Member) = thread.client.buildRestAction<Unit> {
        route = Route.Thread.REMOVE_THREAD_MEMBER(thread.id, member.id).delete()
    }

    suspend operator fun plusAssign(member: Member) = add(member)

    suspend operator fun minusAssign(member: Member) = remove(member)
}