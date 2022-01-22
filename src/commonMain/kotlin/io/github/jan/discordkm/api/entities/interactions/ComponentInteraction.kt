/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.messages.DataMessage
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.entities.messages.MessageBuilder
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

class ComponentInteraction(client: Client, data: JsonObject) : StandardInteraction(client, data) {

    /**
     * The message which contains this component
     */
    val message: Message get() = Message(data["message"]!!.jsonObject, client)

    /**
     * Responds to this interaction with no changes. Use this if you don't want to reply or anything
     */
    suspend fun deferEdit() = client.buildRestAction<Unit> {
        route = Route.Interaction.CALLBACK(id, token).post(buildJsonObject {
            put("type", 6) //defer edit
        })
    }

    /**
     * Edits the original message as callback
     */
    suspend fun edit(message: DataMessage) = client.buildRestAction<Unit> {
        route = Route.Interaction.CALLBACK(id, token).post(buildJsonObject {
            put("type", 7) //edit
            put("data", message.build().toString().toJsonObject())
        })
    }

    /**
     * Edits the original message as callback
     */
    suspend fun edit(message: MessageBuilder.() -> Unit) = edit(MessageBuilder().apply(message).build())

}