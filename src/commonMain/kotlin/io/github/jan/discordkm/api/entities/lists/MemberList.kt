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
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.exceptions.PermissionException
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.put
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.buildJsonObject

class MemberList(override val internalMap: Map<Snowflake, Member>) : NameableSnowflakeList<Member>

class RetrievableMemberList(val guild: Guild, override val internalMap: Map<Snowflake, Member>) : NameableSnowflakeList<Member> {

    suspend fun retrieve(id: Snowflake) = guild.client.buildRestAction<Member> {
        route = Route.Member.GET_MEMBER(guild.id, id).get()
        onFinish { (guild as GuildData).memberCache[it.id] = it }
        transform { it.toJsonObject().extractGuildEntity(guild) }
    }

    /**
     * Retrieves all members an updates the cache
     *
     * **Warning: Requires the GUILD_MEMBERS privileged intent enabled on your application**
     * @param limit The amount of members to retrieve (1-1000)
     * @param after "The highest user id in the previous page"
     */

    suspend fun retrieveMembers(limit: Int = 1, after: Snowflake? = null) = guild.client.buildRestAction<List<Member>> {
        route = Route.Member.GET_MEMBERS(guild.id).get {
            put("limit", limit)
            putOptional("after", after)
        }
        transform { it.toJsonObject().extractGuildEntity(guild) }
        check {
            if(limit < 1 || limit > 1000) throw IllegalArgumentException("The member limit has to be between 1 and 1000")
        }
        onFinish {
            (guild as GuildData).memberCache.internalMap.clear()
            (guild as GuildData).memberCache.internalMap.putAll(it.associateBy { member -> member.id })
        }
    }

    /**
     * Returns all members who start with the given Query
     * @param query The query
     * @param limit The amount of members to retrieve (1-1000)
     */

    suspend fun search(query: String, limit: Int = 1) = guild.client.buildRestAction<List<Member>> {
        route = Route.Member.SEARCH_MEMBERS(guild.id).get {
            put("query", query)
            put("limit", limit)
        }
    }

    /**
     * Kicks the member from the guild.
     *
     * Requires the permission [Permission.KICK_MEMBERS]
     */

    suspend fun kick(memberId: Snowflake) = guild.client.buildRestAction<Unit> {
        route = Route.Member.KICK_MEMBER(guild.id, memberId).delete()
        transform {}
        onFinish { (guild as GuildData).memberCache.remove(memberId) }
        check { if(Permission.KICK_MEMBERS !in guild.selfMember.permissions) throw PermissionException("You require the permission KICK_MEMBERS to kick members from a guild") }
    }

    /**
     * Bans a member from the guild
     *
     * Requires the permission [Permission.BAN_MEMBERS]
     */

    suspend fun ban(userId: Snowflake, delDays: Int? = null) = guild.client.buildRestAction<Unit> {
        route = Route.Ban.CREATE_BAN(guild.id, userId).put(buildJsonObject {
            putOptional("delete_message_days", delDays)
        })
        transform {  }
        check {
            if (delDays != null) {
                if(delDays > 7 || delDays < 0) throw IllegalArgumentException("The delDays have to be between 0 and 7")
            }
            check { if(Permission.BAN_MEMBERS !in guild.selfMember.permissions) throw PermissionException("You require the permission BAN_MEMBERS to ban members from a guild") }
        }
        onFinish { (guild as GuildData).memberCache.remove(userId) }
    }

    /**
     * Unbans a member from the guild
     *
     * Requires the permission [Permission.BAN_MEMBERS]
     */

    suspend fun unban(userId: Snowflake) = guild.client.buildRestAction<Unit> {
        route = Route.Ban.REMOVE_BAN(guild.id, userId).delete()
        transform {  }
        check {
            check { if(Permission.BAN_MEMBERS !in guild.selfMember.permissions) throw PermissionException("You require the permission BAN_MEMBERS to ban members from a guild") }
        }
    }


}

