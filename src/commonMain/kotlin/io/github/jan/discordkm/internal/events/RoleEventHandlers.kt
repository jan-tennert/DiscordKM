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
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.events.RoleCreateEvent
import io.github.jan.discordkm.api.events.RoleDeleteEvent
import io.github.jan.discordkm.api.events.RoleUpdateEvent
import io.github.jan.discordkm.internal.caching.Cache
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.entities.guilds.RoleData
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class RoleCreateEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<RoleCreateEvent> {

    override fun handle(data: JsonObject): RoleCreateEvent {
        val guildId = data.getOrThrow<Snowflake>("guild_id")
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")] ?: throw IllegalStateException("Guild with id $guildId couldn't be found on event GuildMemberUpdateEvent. The guilds probably aren't done initialising.")
        val role = RoleData(guild, data.getValue("role").jsonObject)
        if(Cache.ROLES in client.enabledCache) (guild as GuildData).roleCache[role.id] = role
        return RoleCreateEvent(role)
    }

}

class RoleUpdateEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<RoleUpdateEvent> {

    override fun handle(data: JsonObject): RoleUpdateEvent {
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        val role = RoleData(guild, data.getValue("role").jsonObject)
        val oldRole = guild.roles[role.id]
        if(Cache.ROLES in client.enabledCache) (guild as GuildData).roleCache[role.id] = role
        return RoleUpdateEvent(role, oldRole)
    }

}

class RoleDeleteEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<RoleDeleteEvent> {

    override fun handle(data: JsonObject): RoleDeleteEvent {
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        val roleId = data.getOrThrow<Snowflake>("role_id")
        if(Cache.ROLES in client.enabledCache) (guild as GuildData).roleCache.remove(roleId)
        return RoleDeleteEvent(client, roleId)
    }

}