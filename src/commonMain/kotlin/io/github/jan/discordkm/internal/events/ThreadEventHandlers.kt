/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.guild.GuildTextChannel
import io.github.jan.discordkm.api.entities.channels.guild.StageChannel
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.clients.WSDiscordClientImpl
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.events.ThreadCreateEvent
import io.github.jan.discordkm.api.events.ThreadDeleteEvent
import io.github.jan.discordkm.api.events.ThreadListSyncEvent
import io.github.jan.discordkm.api.events.ThreadMembersUpdateEvent
import io.github.jan.discordkm.api.events.ThreadUpdateEvent
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.snowflake
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

internal class ThreadCreateEventHandler(val client: DiscordClient) : InternalEventHandler<ThreadCreateEvent> {

    override suspend fun handle(data: JsonObject): ThreadCreateEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val thread = Thread(data, guild)
        thread.parent?.let { (client as? WSDiscordClientImpl)?.lastThreads?.set(it.id, thread) }
        guild.cache?.cacheManager?.threadCache?.set(thread.id, thread)
        return ThreadCreateEvent(thread)
    }

}

internal class ThreadUpdateEventHandler(val client: DiscordClient) : InternalEventHandler<ThreadUpdateEvent> {

    override suspend fun handle(data: JsonObject): ThreadUpdateEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val thread = Thread(data, guild)
        val oldThread = guild.cache?.threads?.get(thread.id)
        guild.cache?.cacheManager?.threadCache?.set(thread.id, thread)
        return ThreadUpdateEvent(thread, oldThread)
    }

}

internal class ThreadDeleteEventHandler(val client: DiscordClient) : InternalEventHandler<ThreadDeleteEvent> {

    override suspend fun handle(data: JsonObject): ThreadDeleteEvent {
        val threadId = data["id"]!!.snowflake
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val stageChannel = StageChannel(data["parent_id"]!!.snowflake, guild)
        val type = ChannelType[data["type"]!!.int]
        guild.cache?.cacheManager?.threadCache?.remove(threadId)
        return ThreadDeleteEvent(client, threadId, guild, stageChannel, type)
    }

}

internal class ThreadMembersUpdateEventHandler(val client: DiscordClient) : InternalEventHandler<ThreadMembersUpdateEvent> {

    override suspend fun handle(data: JsonObject): ThreadMembersUpdateEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val thread = Thread(data["id"]!!.snowflake, guild, ChannelType.GUILD_PUBLIC_THREAD)
        val memberCount = data["member_count"]!!.int
        val addedMembers = data["added_members"]!!.jsonArray.map { Thread.ThreadMember(it.jsonObject, guild) }
        val removedMembers = data["removed_member_ids"]!!.jsonArray.map { it.snowflake }
        //cache members
        return ThreadMembersUpdateEvent(thread, memberCount, addedMembers, removedMembers)
    }

}

internal class ThreadListSyncEventHandler(val client: DiscordClient) : InternalEventHandler<ThreadListSyncEvent> {

    override suspend fun handle(data: JsonObject): ThreadListSyncEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val threads = data["threads"]!!.jsonArray.map { Thread(it.jsonObject, guild) }
        val members = data["members"]!!.jsonArray.map { Thread.ThreadMember(it.jsonObject, guild) }
        val channels = data["channels"]!!.jsonArray.map { GuildTextChannel(it.jsonObject["id"]!!.snowflake, guild) }
        return ThreadListSyncEvent(guild, threads, members, channels)
    }

}