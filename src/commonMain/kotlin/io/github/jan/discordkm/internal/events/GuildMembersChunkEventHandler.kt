/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.member.Member
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.events.GuildMembersChunkEvent
import io.github.jan.discordkm.internal.serialization.serializers.GuildSerializer
import io.github.jan.discordkm.internal.utils.get
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.snowflake
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

internal class GuildMembersChunkEventHandler(private val client: DiscordClient) : InternalEventHandler<GuildMembersChunkEvent> {

    override suspend fun handle(data: JsonObject): GuildMembersChunkEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val notFound = data["not_found", true]?.let { it.jsonArray.map { id -> io.github.jan.discordkm.api.entities.User(id.snowflake, client) } } ?: emptyList()
        val members = data["members"]!!.jsonArray.map {
            Member(it.jsonObject, guild)
        }
        val chunkIndex = data["chunk_index"]!!.int
        val chunkCount = data["chunk_count"]!!.int
        val presences = data["presences", true]?.let {
            it.jsonArray.map { presence -> GuildSerializer.deserializeGuildPresence(presence.jsonObject, client) }
        } ?: emptyList()
        guild.cache?.let {
            it.cacheManager.memberCache.putAll(members.associateBy(Member::id))
            it.cacheManager.presences.putAll(presences.associateBy { presence -> presence.user.id })
        }
        return GuildMembersChunkEvent(guild, members, chunkIndex, chunkCount, notFound, presences)
    }

}