/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.GuildCacheEntryImpl
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.entities.guild.member.Member
import io.github.jan.discordkm.api.events.GuildMemberAddEvent
import io.github.jan.discordkm.api.events.GuildMemberRemoveEvent
import io.github.jan.discordkm.api.events.GuildMemberUpdateEvent
import io.github.jan.discordkm.internal.utils.snowflake
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal class GuildMemberAddEventHandler(val client: DiscordClient) : InternalEventHandler<GuildMemberAddEvent> {

    override suspend fun handle(data: JsonObject): GuildMemberAddEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val member = Member(data, guild)
        guild.cache?.cacheManager?.memberCache?.set(member.id, member)
        (guild.cache as GuildCacheEntryImpl).addMember()
        return GuildMemberAddEvent(member)
    }

}

internal class GuildMemberUpdateEventHandler(val client: DiscordClient) : InternalEventHandler<GuildMemberUpdateEvent> {

    override suspend fun handle(data: JsonObject): GuildMemberUpdateEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val member = Member(data, guild)
        guild.cache?.cacheManager?.memberCache?.set(member.id, member)
        return GuildMemberUpdateEvent(member)
    }

}

internal class GuildMemberRemoveEventHandler(val client: DiscordClient) : InternalEventHandler<GuildMemberRemoveEvent> {

    override suspend fun handle(data: JsonObject): GuildMemberRemoveEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val user = User(data["user"]!!.jsonObject, client)
        guild.cache?.cacheManager?.memberCache?.remove(user.id)
        (guild.cache as GuildCacheEntryImpl).removeMember()
        return GuildMemberRemoveEvent(guild, user)
    }

}