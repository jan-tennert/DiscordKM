package io.github.jan.discordkm.api.entities.lists

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.toJsonObject

class UserList(val client: Client, override val internalMap: Map<Snowflake, UserCacheEntry>) : NameableSnowflakeList<UserCacheEntry> {

    suspend fun retrieve(id: Snowflake) = client.buildRestAction<User> {
        route = Route.User.GET_USER(id).get()
        transform { it.toJsonObject().extractClientEntity(client) }
        onFinish { client.userCache[it.id] = it }
    }

}

