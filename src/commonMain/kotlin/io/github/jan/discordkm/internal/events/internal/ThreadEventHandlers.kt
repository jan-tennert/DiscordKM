package io.github.jan.discordkm.internal.events.internal

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.channels.GuildTextChannel
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.api.events.ThreadCreateEvent
import io.github.jan.discordkm.api.events.ThreadDeleteEvent
import io.github.jan.discordkm.api.events.ThreadMembersUpdateEvent
import io.github.jan.discordkm.api.events.ThreadUpdateEvent
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.entities.guilds.channels.ThreadData
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ThreadCreateEventHandler(val client: Client) : InternalEventHandler<ThreadCreateEvent> {

    override fun handle(data: JsonObject): ThreadCreateEvent {
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        val thread = ThreadData(guild, data)
        (guild as GuildData).threadCache[thread.id] = thread
        return ThreadCreateEvent(thread)
    }

}

class ThreadUpdateEventHandler(val client: Client) : InternalEventHandler<ThreadUpdateEvent> {

    override fun handle(data: JsonObject): ThreadUpdateEvent {
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        val thread = ThreadData(guild, data)
        (guild as GuildData).threadCache[thread.id] = thread
        return ThreadUpdateEvent(thread)
    }

}

class ThreadDeleteEventHandler(val client: Client) : InternalEventHandler<ThreadDeleteEvent> {

    override fun handle(data: JsonObject): ThreadDeleteEvent {
        val guildId = data.getOrThrow<Snowflake>("guild_id")
        val guild = client.guilds[guildId]!!
        val id = data.getId()
        val audience = data.getValue("audience").jsonObject
        val channelId = audience.getOrThrow<Snowflake>("parent_id")
        val channel = guild.channels[channelId]!! as GuildTextChannel
        val memberIds = audience.getValue("member_ids").jsonArray.map { Snowflake.fromId(it.jsonPrimitive.content) }
        val members = memberIds.map { guild.members[it]!! }
        (guild as GuildData).threadCache.remove(id)
        return ThreadDeleteEvent(client, id, guildId, guild, channelId, channel, memberIds, members)
    }

}

class ThreadMembersUpdateEventHandler(val client: Client) : InternalEventHandler<ThreadMembersUpdateEvent> {

    override fun handle(data: JsonObject): ThreadMembersUpdateEvent {
        val threadId = data.getId()
        val thread = client.threads[threadId]!!
        val memberCount = data.getOrThrow<Int>("member_count")
        val addedMembers = data["added_members"]?.jsonArray?.map { json -> Thread.ThreadMember(thread.guild, json.jsonObject) } ?: emptyList()
        val removedMembers = data["removed_member_ids"]?.jsonArray?.map { Snowflake.fromId(it.jsonPrimitive.content) } ?: emptyList()
        addedMembers.forEach {
            (thread as ThreadData).memberCache[it.userId] = it
        }
        removedMembers.forEach {
            (thread as ThreadData).memberCache.remove(it)
        }
        return ThreadMembersUpdateEvent(threadId, thread, memberCount, addedMembers, removedMembers)
    }

}