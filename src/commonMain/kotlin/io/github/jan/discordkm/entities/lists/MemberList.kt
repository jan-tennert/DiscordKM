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

