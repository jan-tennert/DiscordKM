package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.MemberCacheEntry
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.exceptions.PermissionException
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.put
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.serializers.MemberSerializer
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

open class GuildMemberContainer(val guild: Guild) {

    /**
     * Retrieves a guild member by its id
     */
    suspend fun retrieve(id: Snowflake) = guild.client.buildRestAction<MemberCacheEntry> {
        route = Route.Member.GET_MEMBER(guild.id, id).get()
        transform { Member.from(it.toJsonObject(), guild) }
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
        transform { members -> members.toJsonArray().map { Member.from(it.jsonObject, guild) } }
        check {
            if(limit < 1 || limit > 1000) throw IllegalArgumentException("The member limit has to be between 1 and 1000")
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
        }
    }

    /**
     * Unbans a member from the guild
     *
     * Requires the permission [Permission.BAN_MEMBERS]
     */
    suspend fun unban(userId: Snowflake) = guild.client.buildRestAction<Unit> {
        route = Route.Ban.REMOVE_BAN(guild.id, userId).delete()
        transform {  }
    }

}

class CacheGuildMemberContainer(guild: Guild, override val values: Collection<MemberCacheEntry>) : GuildMemberContainer(guild), NameableSnowflakeContainer<MemberCacheEntry>
class CacheMemberContainer(override val values: Collection<MemberCacheEntry>) : NameableSnowflakeContainer<MemberCacheEntry>