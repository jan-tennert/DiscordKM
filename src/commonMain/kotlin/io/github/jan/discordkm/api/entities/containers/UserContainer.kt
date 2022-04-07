package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.UserCacheEntry
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.toJsonObject

open class UserContainer(val client: DiscordClient){

    /**
     * Retrieves a [User] by its id.
     */
    suspend fun retrieve(id: Snowflake) = client.buildRestAction<User> {
        route = Route.User.GET_USER(id).get()
        transform { User(it.toJsonObject(), client) }
    }

}

class CacheUserContainer(client: DiscordClient, override val values: Collection<UserCacheEntry>) : NameableSnowflakeContainer<UserCacheEntry>, UserContainer(client)