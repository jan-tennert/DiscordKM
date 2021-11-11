/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.channels.GuildTextChannel
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.api.events.ThreadCreateEvent
import io.github.jan.discordkm.api.events.ThreadDeleteEvent
import io.github.jan.discordkm.api.events.ThreadMembersUpdateEvent
import io.github.jan.discordkm.api.events.ThreadUpdateEvent
import io.github.jan.discordkm.internal.caching.Cache
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.entities.guilds.channels.ThreadData
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ThreadCreateEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<ThreadCreateEvent> {

    override fun handle(data: JsonObject): ThreadCreateEvent {
        val guildId = data.getOrThrow<Snowflake>("guild_id")
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")] ?: throw IllegalStateException("Guild with id $guildId couldn't be found on event GuildMemberUpdateEvent. The guilds probably aren't done initialising.")
        val thread = ThreadData(guild, data)
        if(Cache.THREADS in client.enabledCache) (guild as GuildData).threadCache[thread.id] = thread
        return ThreadCreateEvent(thread)
    }

}

class ThreadUpdateEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<ThreadUpdateEvent> {

    override fun handle(data: JsonObject): ThreadUpdateEvent {
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        val thread = ThreadData(guild, data)
        val oldThread = guild.threads[thread.id]
        if(Cache.THREADS in client.enabledCache) (guild as GuildData).threadCache[thread.id] = thread
        return ThreadUpdateEvent(thread, oldThread)
    }

}

class ThreadDeleteEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<ThreadDeleteEvent> {

    override fun handle(data: JsonObject): ThreadDeleteEvent {
        val guildId = data.getOrThrow<Snowflake>("guild_id")
        val guild = client.guilds[guildId]!!
        val id = data.getId()
        val audience = data.getValue("audience").jsonObject
        val channelId = audience.getOrThrow<Snowflake>("parent_id")
        val channel = guild.channels[channelId]!! as GuildTextChannel
        val memberIds = audience.getValue("member_ids").jsonArray.map { Snowflake.fromId(it.jsonPrimitive.content) }
        val members = memberIds.map { guild.members[it]!! }
        if(Cache.THREADS in client.enabledCache) (guild as GuildData).threadCache.remove(channelId)
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
            (thread as ThreadData).memberCache[it.id] = it
        }
        removedMembers.forEach {
            (thread as ThreadData).memberCache.remove(it)
        }
        return ThreadMembersUpdateEvent(threadId, thread, memberCount, addedMembers, removedMembers)
    }

}