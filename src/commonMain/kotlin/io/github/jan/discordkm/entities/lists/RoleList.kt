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

