/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
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
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

interface Invitable : SnowflakeEntity, BaseEntity {

    /*
     * Retrieves all invites for this channel
     */
    suspend fun retrieveInvites() = client.buildRestAction<List<Invite>> {
        route = Route.Invite.GET_CHANNEL_INVITES(id).get()
        transform { json -> json.toJsonArray().map { Invite(client, json.toJsonObject()) } }
    }

    /*
     * Creates a new invite for this channel
     * @param reason The reason which will be displayed in the audit logs
     */
    suspend fun createInvite(reason: String? = null, builder: InviteBuilder.() -> Unit) = client.buildRestAction<Invite> {
        route = Route.Invite.CREATE_CHANNEL_INVITE(id).post(Json.encodeToJsonElement(InviteBuilder().apply(builder).data))
        transform { Invite(client, it.toJsonObject()) }
        this.reason = reason
    }

}