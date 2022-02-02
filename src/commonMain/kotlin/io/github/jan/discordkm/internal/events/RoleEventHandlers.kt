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
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.events.RoleCreateEvent
import io.github.jan.discordkm.api.events.RoleDeleteEvent
import io.github.jan.discordkm.api.events.RoleUpdateEvent
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.snowflake
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class RoleCreateEventHandler(val client: Client) : InternalEventHandler<RoleCreateEvent> {

    override suspend fun handle(data: JsonObject): RoleCreateEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val role = Role(data["role"]!!.jsonObject, guild)
        guild.cache?.cacheManager?.roleCache?.set(role.id, role)
        return RoleCreateEvent(role)
    }

}

class RoleUpdateEventHandler(val client: Client) : InternalEventHandler<RoleUpdateEvent> {

    override suspend fun handle(data: JsonObject): RoleUpdateEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val role = Role(data["role"]!!.jsonObject, guild)
        val oldRole = guild.cache?.cacheManager?.roleCache?.get(role.id)
        guild.cache?.cacheManager?.roleCache?.set(role.id, role)
        return RoleUpdateEvent(role, oldRole)
    }

}

class RoleDeleteEventHandler(val client: Client) : InternalEventHandler<RoleDeleteEvent> {

    override suspend fun handle(data: JsonObject): RoleDeleteEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val roleId = data.getOrThrow<Snowflake>("role_id")
        guild.cache?.cacheManager?.roleCache?.remove(roleId)
        return RoleDeleteEvent(client, roleId)
    }

}