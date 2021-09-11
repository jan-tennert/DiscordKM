package io.github.jan.discordkm.entities.lists

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractClientEntity
import io.github.jan.discordkm.utils.toJsonObject

class GuildList(val client: Client, override val internalList: List<Guild>) : DiscordList<Guild> {

    override fun get(name: String) = internalList.filter { it.name == name }

    suspend fun retrieve(id: Snowflake) = client.buildRestAction<Guild> {
        action = RestAction.Action.get("/guilds/$id")
        transform { it.toJsonObject().extractClientEntity(client) }
    }

}

