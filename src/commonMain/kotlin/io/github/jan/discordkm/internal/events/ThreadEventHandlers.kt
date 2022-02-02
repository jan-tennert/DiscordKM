/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.guild.StageChannel
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.events.ThreadCreateEvent
import io.github.jan.discordkm.api.events.ThreadDeleteEvent
import io.github.jan.discordkm.api.events.ThreadMembersUpdateEvent
import io.github.jan.discordkm.api.events.ThreadUpdateEvent
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.snowflake
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class ThreadCreateEventHandler(val client: Client) : InternalEventHandler<ThreadCreateEvent> {

    override suspend fun handle(data: JsonObject): ThreadCreateEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val thread = Thread(data, guild)
        guild.cache?.cacheManager?.threadCache?.set(thread.id, thread)
        return ThreadCreateEvent(thread)
    }

}

class ThreadUpdateEventHandler(val client: Client) : InternalEventHandler<ThreadUpdateEvent> {

    override suspend fun handle(data: JsonObject): ThreadUpdateEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val thread = Thread(data, guild)
        val oldThread = guild.cache?.threads?.get(thread.id)
        guild.cache?.cacheManager?.threadCache?.set(thread.id, thread)
        return ThreadUpdateEvent(thread, oldThread)
    }

}

class ThreadDeleteEventHandler(val client: Client) : InternalEventHandler<ThreadDeleteEvent> {

    override suspend fun handle(data: JsonObject): ThreadDeleteEvent {
        val threadId = data["id"]!!.snowflake
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val stageChannel = StageChannel(data["parent_id"]!!.snowflake, guild)
        val type = ChannelType[data["type"]!!.int]
        guild.cache?.cacheManager?.threadCache?.remove(threadId)
        return ThreadDeleteEvent(client, threadId, guild, stageChannel, type)
    }

}

class ThreadMembersUpdateEventHandler(val client: Client) : InternalEventHandler<ThreadMembersUpdateEvent> {

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