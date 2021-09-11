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
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.toJsonObject

sealed interface IMemberList : DiscordList<Member> {

    override fun get(name: String) = internalList.filter { it.user.name == name }

}

class MemberList(override val internalList: List<Member>) : IMemberList

class RetrievableMemberList(val guild: Guild, override val internalList: List<Member>) : IMemberList {

    override fun get(name: String) = internalList.filter { it.user.name == name }

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


}
