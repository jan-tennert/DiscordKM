/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.message.DataMessage
import io.github.jan.discordkm.api.entities.message.Message
import io.github.jan.discordkm.api.entities.message.MessageBuilder
import io.github.jan.discordkm.api.entities.message.buildMessage
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.put
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

class ComponentInteraction(client: DiscordClient, data: JsonObject) : StandardInteraction(client, data), ModalInteraction {

    /*
     * The message which contains this component
     */
    val message: Message get() = Message(data["message"]!!.jsonObject, client)

    /*
     * Responds to this interaction with no changes. Use this if you don't want to reply or anything
     */
    suspend fun deferEdit() = client.buildRestAction<Unit> {
        route = Route.Interaction.CALLBACK(id, token).post(buildJsonObject {
            put("type", InteractionCallbackType.DEFERRED_UPDATE_MESSAGE)
        })
    }

    /*
     * Edits the original message as callback
     */
    suspend fun edit(message: DataMessage) = client.buildRestAction<Unit> {
        route = Route.Interaction.CALLBACK(id, token).post(buildJsonObject {
            put("type", InteractionCallbackType.UPDATE_MESSAGE)
            put("data", message.build().toString().toJsonObject())
        })
    }

    /*
     * Edits the original message as callback
     */
    suspend fun edit(message: MessageBuilder.() -> Unit) = edit(buildMessage(client, message))

}