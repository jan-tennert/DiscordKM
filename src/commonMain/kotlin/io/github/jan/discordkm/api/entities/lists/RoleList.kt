/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.lists

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.guild.RoleModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.entities.guilds.MemberData
import io.github.jan.discordkm.internal.entities.guilds.RoleData
import io.github.jan.discordkm.internal.exceptions.PermissionException
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.put
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.jsonObject

interface IRoleList : DiscordList<Role> {

    override fun get(name: String) = internalList.filter { it.name == name }

}

class RoleList(val guild: Guild, override val internalList: List<Role>) : IRoleList {


    suspend fun retrieveRoles()  = guild.client.buildRestAction<List<Role>> {
        route = Route.Role.GET_ROLES(guild.id).get()
        transform {
            it.toJsonArray().map { json -> json.jsonObject.extractGuildEntity<Role>(guild) }
        }
        onFinish {
            (guild as GuildData).roleCache.internalMap.clear()
            (guild as GuildData).roleCache.internalMap.putAll(it.associateBy { role -> role.id })
        }
    }

    /**
     * Creates a role
     *
     * Requires the permission [Permission.MANAGE_ROLES]
     */

    suspend fun create(builder: RoleModifier.() -> Unit) = guild.client.buildRestAction<Role> {
        route = Route.Role.CREATE_ROLE(guild.id).post(RoleModifier().apply(builder).build())
        transform { RoleData(guild, it.toJsonObject()) }
        onFinish { (guild as GuildData).roleCache[it.id] = it }
        check { if(Permission.MANAGE_ROLES !in guild.selfMember.permissions) throw PermissionException("You need the permission MANAGE_ROLES to create roles") }
    }

}

class RetrievableRoleList(val member: Member, override val internalList: List<Role>) : IRoleList {


    suspend fun add(role: Role) = add(role.id)

    suspend fun remove(role: Role) = remove(role.id)

    suspend fun add(roleId: Snowflake) = member.client.buildRestAction<Unit> {
        route = Route.Member.ADD_ROLE_TO_MEMBER(member.guild.id, member.id, roleId).put()
        transform {  }
        onFinish { member.guild.roles[roleId]?.let {
            (member as MemberData).roleCache[it.id] = it
        } }
    }


    suspend fun remove(roleId: Snowflake) = member.client.buildRestAction<Unit> {
        route = Route.Member.REMOVE_ROLE_FROM_MEMBER(member.guild.id, member.id, roleId).delete()
        transform {  }
        onFinish { if(member.roles.contains(roleId)) (member as MemberData).roleCache.remove(roleId) }
    }



    suspend operator fun plusAssign(role: Role) = add(role)


    suspend operator fun minusAssign(role: Role) = remove(role)


    suspend operator fun plusAssign(roleId: Snowflake) = add(roleId)


    suspend operator fun minusAssign(roleId: Snowflake) = remove(roleId)

}

class UserList(val client: Client, override val internalList: List<User>) : DiscordList<User> {

    override fun get(name: String) = internalList.filter { it.name == name }


    suspend fun retrieve(id: Snowflake) = client.buildRestAction<User> {
        route = Route.User.GET_USER(id).get()
        transform { it.toJsonObject().extractClientEntity(client) }
        onFinish { client.userCache[it.id] = it }
    }

}

