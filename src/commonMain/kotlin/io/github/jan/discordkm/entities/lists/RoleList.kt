/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.lists

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.User
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.entities.guild.Role
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractClientEntity
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.toJsonArray
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.jsonObject

class RoleList(val guild: Guild, override val internalList: List<Role>) : DiscordList<Role> {

    override fun get(name: String) = internalList.filter { it.name == name }

    suspend fun retrieve(id: Snowflake)  = guild.client.buildRestAction<Role> {
        action = RestAction.Action.get("/guilds/${guild.id}/roles")
        transform {
            val roles = it.toJsonArray().map { json -> json.jsonObject.extractGuildEntity<Role>(guild) }
            guild.roleCache.internalMap.clear()
            guild.roleCache.internalMap.putAll(roles.associateBy { role -> role.id })
            roles.first { role -> role.id == id }
        }
    }
}

class UserList(val client: Client, override val internalList: List<User>) : DiscordList<User> {

    override fun get(name: String) = internalList.filter { it.name == name }

    suspend fun retrieve(id: Snowflake) = client.buildRestAction<User> {
        action = RestAction.Action.get("/users/$id")
        transform { it.toJsonObject().extractClientEntity(client) }
        onFinish { client.userCache[it.id] = it }
    }

}

