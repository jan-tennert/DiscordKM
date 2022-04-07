/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.role.RoleCacheEntry

interface RoleEvent : GuildEvent {

    val role: RoleCacheEntry

}

/**
 * Sent when a role was created
 */
class RoleCreateEvent(override val role: RoleCacheEntry) : RoleEvent, RoleCacheEntry by role {

    override val client: DiscordClient
        get() = role.client

}

/**
 * Sent when a role was updated
 */
class RoleUpdateEvent(override val role: RoleCacheEntry, val oldRole: RoleCacheEntry?) : RoleEvent, RoleCacheEntry by role {

    override val client: DiscordClient
        get() = role.client

}

/**
 * Sent when a role was deleted
 */
class RoleDeleteEvent(override val client: DiscordClient, val roleId: Snowflake) : Event