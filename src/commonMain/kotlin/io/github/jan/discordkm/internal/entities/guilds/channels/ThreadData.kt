package io.github.jan.discordkm.internal.entities.guilds.channels

import io.github.jan.discordkm.Cache
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.api.entities.guild.channels.modifier.ThreadModifier
import io.github.jan.discordkm.api.entities.lists.ThreadMemberList
import io.github.jan.discordkm.api.entities.messages.DataMessage
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.restaction.RestAction
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.extractMessageChannelEntity
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class ThreadData(guild: Guild, data: JsonObject, members: List<Thread.ThreadMember> = emptyList()) : GuildTextChannelData(guild, data), Thread {

    val memberCache = Cache.fromSnowflakeEntityList(members)

    override val members: ThreadMemberList
        get() = ThreadMemberList(this, memberCache.values)

    override suspend fun join() = client.buildRestAction<Unit> {
        action = RestAction.put("/channels/$id/thread-members/@me")
        transform {  }
        check { if(metadata.isArchived) throw UnsupportedOperationException("This thread is archived. You can't join anymore ") }
    }

    override suspend fun retrieveThreadMembers() = client.buildRestAction<List<Thread.ThreadMember>> {
        action = RestAction.get("/channels/$id/thread-members")
        transform { it.toJsonArray().map { json -> Thread.ThreadMember(guild, json.jsonObject) }}
        onFinish { memberCache.internalMap.clear(); memberCache.internalMap.putAll(it.associateBy { member -> member.id }) }
    }

    override suspend fun leave() = client.buildRestAction<Unit> {
        action = RestAction.delete("/channels/$id/thread-members/@me")
        transform {  }
    }

    override suspend fun send(message: DataMessage) = client.buildRestAction<Message> {
        action = RestAction.post("/channels/$id/messages", Json.encodeToString(message))
        transform {
            it.toJsonObject().extractMessageChannelEntity(this@ThreadData)
        }
        check { if(metadata.isArchived) throw UnsupportedOperationException("This thread is archived. You can send messages in it anymore ")}
    }

    override suspend fun modify(modifier: ThreadModifier.() -> Unit) = client.buildRestAction<Thread> {
        action = RestAction.patch("/channels/${id}", ThreadModifier(this@ThreadData).apply(modifier).build())
        transform { it.toJsonObject().extractGuildEntity(guild) }
        onFinish {
            (guild as GuildData).threadCache[id] = it
        }
    }

}