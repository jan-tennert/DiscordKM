package io.github.jan.discordkm.internal.entities.channels

import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.guild.invites.Invite
import io.github.jan.discordkm.api.entities.guild.invites.InviteBuilder
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

interface Invitable : SnowflakeEntity, BaseEntity {

    /**
     * Retrieves all invites for this channel
     */
    suspend fun retrieveInvites() = client.buildRestAction<List<Invite>> {
        route = Route.Invite.GET_CHANNEL_INVITES(id).get()
        transform { json -> json.toJsonArray().map { it.jsonObject.extractClientEntity(client) } }
    }

    /**
     * Creates a new invite for this channel
     */
    suspend fun createInvite(builder: InviteBuilder.() -> Unit) = client.buildRestAction<Invite> {
        route = Route.Invite.CREATE_CHANNEL_INVITE(id).post(Json.encodeToJsonElement(InviteBuilder().apply(builder).build()))
        transform { Invite(client, it.toJsonObject()) }
    }

}