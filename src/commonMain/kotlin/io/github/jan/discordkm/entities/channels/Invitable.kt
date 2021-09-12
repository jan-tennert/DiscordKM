package io.github.jan.discordkm.entities.channels

import io.github.jan.discordkm.entities.BaseEntity
import io.github.jan.discordkm.entities.SerializableEntity
import io.github.jan.discordkm.entities.SnowflakeEntity
import io.github.jan.discordkm.entities.guild.invites.Invite
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extractClientEntity
import io.github.jan.discordkm.utils.toJsonArray
import kotlinx.serialization.json.jsonObject

interface Invitable : SnowflakeEntity, BaseEntity {

    suspend fun retrieveInvites() = client.buildRestAction<List<Invite>> {
        action = RestAction.Action.get("/channels/$id/invites")
        transform { json -> json.toJsonArray().map { it.jsonObject.extractClientEntity(client) } }
    }

}