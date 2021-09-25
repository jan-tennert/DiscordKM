package io.github.jan.discordkm.internal.entities.channels

import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.guild.invites.Invite

import io.github.jan.discordkm.internal.restaction.RestAction
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.toJsonArray
import kotlinx.serialization.json.jsonObject

interface Invitable : SnowflakeEntity, BaseEntity {


    suspend fun retrieveInvites() = client.buildRestAction<List<Invite>> {
        action = RestAction.get("/channels/$id/invites")
        transform { json -> json.toJsonArray().map { it.jsonObject.extractClientEntity(client) } }
    }

}