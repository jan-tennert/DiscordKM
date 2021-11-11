/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.entities.guilds.channels

import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.api.entities.guild.channels.modifier.ThreadModifier
import io.github.jan.discordkm.api.entities.lists.ThreadMemberList
import io.github.jan.discordkm.api.entities.messages.DataMessage
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.internal.caching.EntityCache
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.put
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.extractMessageChannelEntity
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class ThreadData(guild: Guild, data: JsonObject, members: List<Thread.ThreadMember> = emptyList()) : GuildTextChannelData(guild, data), Thread {

    val memberCache = EntityCache.fromSnowflakeEntityList(members)

    override val members: ThreadMemberList
        get() = ThreadMemberList(this, memberCache.values.associateBy { it.id })

    override suspend fun join() = client.buildRestAction<Unit> {
        route = Route.Thread.JOIN_THREAD(id).put()
        transform {  }
        check { if(metadata.isArchived) throw UnsupportedOperationException("This thread is archived. You can't join anymore ") }
    }

    override suspend fun retrieveThreadMembers() = client.buildRestAction<List<Thread.ThreadMember>> {
        route = Route.Thread.GET_THREAD_MEMBERS(id).get()
        transform { it.toJsonArray().map { json -> Thread.ThreadMember(guild, json.jsonObject) }}
        onFinish { memberCache.internalMap.clear(); memberCache.internalMap.putAll(it.associateBy { member -> member.id }) }
    }

    override suspend fun leave() = client.buildRestAction<Unit> {
        route = Route.Thread.LEAVE_THREAD(id).delete()
        transform {  }
    }

    override suspend fun send(message: DataMessage) = client.buildRestAction<Message> {
        route = Route.Message.CREATE_MESSAGE(id).post(message.build())
        transform {
            it.toJsonObject().extractMessageChannelEntity(this@ThreadData)
        }
        check { if(metadata.isArchived) throw UnsupportedOperationException("This thread is archived. You can send messages in it anymore ")}
    }

    override suspend fun modify(modifier: ThreadModifier.() -> Unit) = client.buildRestAction<Thread> {
        route = Route.Channel.MODIFY_CHANNEL(id).patch(ThreadModifier(this@ThreadData).apply(modifier).build())
        transform { it.toJsonObject().extractGuildEntity(guild) }
        onFinish {
            (guild as GuildData).threadCache[id] = it
        }
    }

}