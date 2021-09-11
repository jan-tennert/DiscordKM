package com.github.jan.discordkm.entities.misc

import com.github.jan.discordkm.Client
import com.github.jan.discordkm.entities.Snowflake
import com.github.jan.discordkm.entities.User
import com.github.jan.discordkm.entities.guild.Guild
import com.github.jan.discordkm.entities.guild.Member
import com.github.jan.discordkm.entities.guild.Role
import com.github.jan.discordkm.entities.guild.channels.Category
import com.github.jan.discordkm.entities.guild.channels.GuildChannel
import com.github.jan.discordkm.entities.guild.channels.NewsChannel
import com.github.jan.discordkm.entities.guild.channels.StageChannel
import com.github.jan.discordkm.entities.guild.channels.TextChannel
import com.github.jan.discordkm.entities.guild.channels.VoiceChannel
import com.github.jan.discordkm.restaction.RestAction
import com.github.jan.discordkm.restaction.buildRestAction
import com.github.jan.discordkm.utils.extractClientEntity
import com.github.jan.discordkm.utils.extractGuildEntity
import com.github.jan.discordkm.utils.toJsonArray
import com.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.jsonObject

sealed interface DiscordList <T : Snowflake> : Iterable<T> {

    val internalList: List<T>

    /**
     * Gets this object from the cache
     */
    operator fun get(id: Long) = internalList.firstOrNull { it.id == id }

    /**
     * Searches for the object in the cache and returns a list of matching objects
     */
    operator fun get(name: String) : List<T>

    override operator fun iterator() = internalList.iterator()

}

class RoleList(val guild: Guild, override val internalList: List<Role>) : DiscordList<Role> {

    override fun get(name: String) = internalList.filter { it.name == name }

    suspend fun retrieve(id: Long)  = guild.client.buildRestAction<Role> {
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

    suspend fun retrieve(id: Long) = client.buildRestAction<User> {
        action = RestAction.Action.get("/users/$id")
        transform { it.toJsonObject().extractClientEntity(client) }
        onFinish { client.userCache[it.id] = it }
    }

}

class MemberList(val guild: Guild, override val internalList: List<Member>) : DiscordList<Member> {

    override fun get(name: String) = internalList.filter { it.user.name == name }

    suspend fun retrieve(id: Long) = guild.client.buildRestAction<Member> {
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
            if(limit < 1 || limit > 1000) return@check IllegalArgumentException("The member limit has to be between 1 and 1000")
            null
        }
        onFinish {
            guild.memberCache.internalMap.clear()
            guild.memberCache.internalMap.putAll(it.associateBy { member -> member.id })
        }
    }


}

class GuildList(val client: Client, override val internalList: List<Guild>) : DiscordList<Guild> {

    override fun get(name: String) = internalList.filter { it.name == name }

    suspend fun retrieve(id: Long) = client.buildRestAction<Guild> {
        action = RestAction.Action.get("/guilds/$id")
        transform { it.toJsonObject().extractClientEntity(client) }
    }

}

class ChannelList(val guild: Guild, override val internalList: List<GuildChannel>) : DiscordList<GuildChannel> {

    override fun get(name: String) = internalList.filter { it.name == name }

    inline fun <reified C : GuildChannel> getGuildChannel(id: Long) = when(C::class) {
        VoiceChannel::class -> get(id) as C
        StageChannel::class -> get(id) as C
        TextChannel::class -> get(id) as C
        Category::class -> get(id) as C
        NewsChannel::class -> get(id) as C
        else -> throw IllegalStateException()
    }

    suspend inline fun <reified T : GuildChannel> retrieve(id: Long) = guild.client.buildRestAction<T> {
        action = RestAction.Action.get("/channels/$id")
        transform { it.toJsonObject().extractGuildEntity(guild) }
        onFinish { guild.channelCache[id] = it }
    }
}