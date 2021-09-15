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
import io.github.jan.discordkm.entities.guild.Member
import io.github.jan.discordkm.entities.guild.Permission
import io.github.jan.discordkm.entities.guild.Role
import io.github.jan.discordkm.entities.guild.RoleModifier
import io.github.jan.discordkm.exceptions.PermissionException
import io.github.jan.discordkm.restaction.CallsTheAPI
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractClientEntity
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.toJsonArray
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.jsonObject

interface IRoleList : DiscordList<Role> {

    override fun get(name: String) = internalList.filter { it.name == name }

}

class RoleList(val guild: Guild, override val internalList: List<Role>) : IRoleList {

    @CallsTheAPI
    suspend fun retrieveRoles()  = guild.client.buildRestAction<List<Role>> {
        action = RestAction.Action.get("/guilds/${guild.id}/roles")
        transform {
            it.toJsonArray().map { json -> json.jsonObject.extractGuildEntity<Role>(guild) }
        }
        onFinish {
            guild.roleCache.internalMap.clear()
            guild.roleCache.internalMap.putAll(it.associateBy { role -> role.id })
        }
    }

    /**
     * Creates a role
     *
     * Requires the permission [Permission.MANAGE_ROLES]
     */
    @CallsTheAPI
    suspend fun create(builder: RoleModifier.() -> Unit) = guild.client.buildRestAction<Role> {
        action = RestAction.Action.post("/guilds/${guild.id}/roles", RoleModifier().apply(builder).build())
        transform { Role(guild, it.toJsonObject()) }
        onFinish { guild.roleCache[it.id] = it }
        check { if(Permission.MANAGE_ROLES !in guild.selfMember.permissions) throw PermissionException("You need the permission MANAGE_ROLES to create roles") }
    }

}

class RetrievableRoleList(val member: Member, override val internalList: List<Role>) : IRoleList {

    @CallsTheAPI
    suspend fun add(role: Role) = add(role.id)

    @CallsTheAPI
    suspend fun remove(role: Role) = remove(role.id)

    @CallsTheAPI
    suspend fun add(roleId: Snowflake) = member.client.buildRestAction<Unit> {
        action = RestAction.Action.put("/guilds/${member.guild.id}/members/${member.id}/roles/${roleId}")
        transform {  }
        onFinish { member.guild.roles[roleId]?.let {
            member.roleCache[it.id] = it
        } }
    }

    @CallsTheAPI
    suspend fun remove(roleId: Snowflake) = member.client.buildRestAction<Unit> {
        action = RestAction.Action.put("/guilds/${member.guild.id}/members/${member.id}/roles/${roleId}")
        transform {  }
        onFinish { if(member.roles.contains(roleId)) member.roleCache.remove(roleId) }
    }


    @CallsTheAPI
    suspend operator fun plusAssign(role: Role) = add(role)

    @CallsTheAPI
    suspend operator fun minusAssign(role: Role) = remove(role)

    @CallsTheAPI
    suspend operator fun plusAssign(roleId: Snowflake) = add(roleId)

    @CallsTheAPI
    suspend operator fun minusAssign(roleId: Snowflake) = remove(roleId)

}

class UserList(val client: Client, override val internalList: List<User>) : DiscordList<User> {

    override fun get(name: String) = internalList.filter { it.name == name }

    @CallsTheAPI
    suspend fun retrieve(id: Snowflake) = client.buildRestAction<User> {
        action = RestAction.Action.get("/users/$id")
        transform { it.toJsonObject().extractClientEntity(client) }
        onFinish { client.userCache[it.id] = it }
    }

}

