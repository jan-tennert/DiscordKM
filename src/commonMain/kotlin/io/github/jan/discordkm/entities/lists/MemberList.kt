/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.lists

import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.entities.guild.Member
import io.github.jan.discordkm.entities.guild.Permission
import io.github.jan.discordkm.exceptions.PermissionException
import io.github.jan.discordkm.restaction.CallsTheAPI
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildQuery
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.putOptional
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.buildJsonObject

sealed interface IMemberList : DiscordList<Member> {

    override fun get(name: String) = internalList.filter { it.user.name == name }

}

class MemberList(override val internalList: List<Member>) : IMemberList

class RetrievableMemberList(val guild: Guild, override val internalList: List<Member>) : IMemberList {

    override fun get(name: String) = internalList.filter { it.user.name == name }

    @CallsTheAPI
    suspend fun retrieve(id: Snowflake) = guild.client.buildRestAction<Member> {
        action = RestAction.Action.get("/guilds/${guild.id}/members/$id")
        onFinish { guild.memberCache[it.id] = it }
        transform { it.toJsonObject().extractGuildEntity(guild) }
    }

    /**
     * Retrieves all members an updates the cache
     *
     * **Warning: Requires the GUILD_MEMBERS privileged intent enabled on your application**
     * @param limit The amount of members to retrieve (1-1000)
     * @param after "The highest user id in the previous page"
     */
    @CallsTheAPI
    suspend fun retrieveMembers(limit: Int = 1, after: Long = 0) = guild.client.buildRestAction<List<Member>> {
        action = RestAction.Action.get("/guilds/${guild.id}/members?limit=$limit&after=$after")
        transform { it.toJsonObject().extractGuildEntity(guild) }
        check {
            if(limit < 1 || limit > 1000) throw IllegalArgumentException("The member limit has to be between 1 and 1000")
        }
        onFinish {
            guild.memberCache.internalMap.clear()
            guild.memberCache.internalMap.putAll(it.associateBy { member -> member.id })
        }
    }

    /**
     * Returns all members who start with the given Query
     * @param query The query
     * @param limit The amount of members to retrieve (1-1000)
     */
    @CallsTheAPI
    suspend fun search(query: String, limit: Int = 1) = guild.client.buildRestAction<List<Member>> {
        action = RestAction.Action.get("/guilds/${guild.id}/members/search" + buildQuery {
            put("query", query)
            put("limit", limit)
        })
    }

    /**
     * Kicks the member from the guild.
     *
     * Requires the permission [Permission.KICK_MEMBERS]
     */
    @CallsTheAPI
    suspend fun kick(memberId: Snowflake) = guild.client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/guilds/${guild.id}/members/$memberId")
        transform {}
        onFinish { guild.memberCache.remove(memberId) }
        check { if(Permission.KICK_MEMBERS !in guild.selfMember.permissions) throw PermissionException("You require the permission KICK_MEMBERS to kick members from a guild") }
    }

    /**
     * Bans a member from the guild
     *
     * Requires the permission [Permission.BAN_MEMBERS]
     */
    @CallsTheAPI
    suspend fun ban(userId: Snowflake, delDays: Int? = null) = guild.client.buildRestAction<Unit> {
        action = RestAction.Action.put("/guilds/${guild.id}/bans/$userId", buildJsonObject {
            putOptional("delete_message_days", delDays)
        })
        transform {  }
        check {
            if (delDays != null) {
                if(delDays > 7 || delDays < 0) throw IllegalArgumentException("The delDays have to be between 0 and 7")
            }
            check { if(Permission.BAN_MEMBERS !in guild.selfMember.permissions) throw PermissionException("You require the permission BAN_MEMBERS to ban members from a guild") }
        }
        onFinish { guild.memberCache.remove(userId) }
    }

    /**
     * Unbans a member from the guild
     *
     * Requires the permission [Permission.BAN_MEMBERS]
     */
    @CallsTheAPI
    suspend fun unban(userId: Snowflake) = guild.client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/guilds/${guild.id}/bans/${userId}")
        transform {  }
        check {
            check { if(Permission.BAN_MEMBERS !in guild.selfMember.permissions) throw PermissionException("You require the permission BAN_MEMBERS to ban members from a guild") }
        }
    }


}

