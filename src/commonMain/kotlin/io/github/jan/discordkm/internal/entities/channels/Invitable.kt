package io.github.jan.discordkm.internal.entities.channels

import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.guild.invites.Invite
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.toJsonArray
import kotlinx.serialization.json.jsonObject

interface Invitable : SnowflakeEntity, BaseEntity {


    suspend fun retrieveInvites() = client.buildRestAction<List<Invite>> {
        route = Route.Invite.GET_CHANNEL_INVITES(id).get()
        transform { json -> json.toJsonArray().map { it.jsonObject.extractClientEntity(client) } }
    }

}