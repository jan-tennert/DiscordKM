/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.member.Member
import io.github.jan.discordkm.api.entities.guild.role.Role
import io.github.jan.discordkm.api.entities.guild.role.RoleCacheEntry
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.entities.modifier.guild.RoleModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.put
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.RoleSerializer
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.jsonObject


open class GuildRoleContainer(val guild: Guild) {

    /*
     * Retrieves all roles in this guild
     */
    suspend fun retrieveRoles()  = guild.client.buildRestAction<List<RoleCacheEntry>> {
        route = Route.Role.GET_ROLES(guild.id).get()
        transform { it.toJsonArray().map { data -> Role(data.jsonObject, guild) } }
        onFinish { roles ->
            val cache = guild.cache?.cacheManager?.roleCache
            cache?.clear()
            cache?.putAll(roles.associateBy { it.id })
        }
    }

    /*
     * Creates a role
     *
     * Requires the permission [Permission.MANAGE_ROLES]
     */
    suspend fun create(reason: String? = null, builder: RoleModifier.() -> Unit) = guild.client.buildRestAction<Role> {
        route = Route.Role.CREATE_ROLE(guild.id).post(RoleModifier().apply(builder).data)
        transform { RoleSerializer.deserialize(it.toJsonObject(), guild) }
        this.reason = reason
    }

}

class CacheGuildRoleContainer(guild: Guild, override val values: Collection<RoleCacheEntry>) : GuildRoleContainer(guild), NameableSnowflakeContainer<RoleCacheEntry>

open class MemberRoleContainer(val member: Member) {

    suspend fun add(role: Role) = add(role.id)

    suspend fun remove(role: Role) = remove(role.id)

    suspend fun add(roleId: Snowflake) = member.client.buildRestAction<Unit> {
        route = Route.Member.ADD_ROLE_TO_MEMBER(member.guild.id, member.id, roleId).put()
        transform {  }
    }


    suspend fun remove(roleId: Snowflake) = member.client.buildRestAction<Unit> {
        route = Route.Member.REMOVE_ROLE_FROM_MEMBER(member.guild.id, member.id, roleId).delete()
        transform {  }
    }

    suspend operator fun plusAssign(role: Role) = add(role)


    suspend operator fun minusAssign(role: Role) = remove(role)


    suspend operator fun plusAssign(roleId: Snowflake) = add(roleId)


    suspend operator fun minusAssign(roleId: Snowflake) = remove(roleId)

}

class CacheMemberRoleContainer(member: Member, override val values: Collection<Role>) : MemberRoleContainer(member), SnowflakeContainer<Role>