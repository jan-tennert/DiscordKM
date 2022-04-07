/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.messages.DataMessage
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.entities.messages.MessageBuilder
import io.github.jan.discordkm.api.entities.messages.MessageCacheEntry
import io.github.jan.discordkm.api.entities.messages.buildMessage
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.put
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

open class StandardInteraction(override val client: DiscordClient, override val data: JsonObject) : Interaction {

    /**
     * Replies to this interaction without a message
     */
    suspend fun deferReply(ephemeral: Boolean = false) = client.buildRestAction<Unit> {
        route = Route.Interaction.CALLBACK(id, token).post(buildJsonObject {
            put("type", InteractionCallbackType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE)
            if (ephemeral) {
                put("data", buildJsonObject {
                    put("flags", 1 shl 6)
                })
            }
        })
    }

    /**
     * Replies to this interaction with a message
     */
    suspend fun reply(message: DataMessage, ephemeral: Boolean = false) = client.buildRestAction<Unit> {
        route = Route.Interaction.CALLBACK(id, token).post(message.buildCallback(InteractionCallbackType.CHANNEL_MESSAGE_WITH_SOURCE.value, ephemeral))
    }

    /**
     * Replies to this interaction with a message
     */
    suspend fun reply(ephemeral: Boolean = false, message: MessageBuilder.() -> Unit) =
        reply(buildMessage(client, message), ephemeral)

    /**
     * Replies to this interaction with a message
     */
    suspend fun reply(message: String, ephemeral: Boolean = false) =
        reply(buildMessage(client) { content = message }, ephemeral)

    /**
     * Edits the original reply message
     */
    suspend fun editOriginalMessage(message: DataMessage) = client.buildRestAction<MessageCacheEntry> {
        route = Route.Interaction.EDIT_ORIGINAL(applicationId, token).patch(message.build(enableStickers = false))
        transform { Message(it.toJsonObject(), client) }
    }

    /**
     * Edits the original reply message
     */
    suspend fun editOriginalMessage(builder: MessageBuilder.() -> Unit) =
        editOriginalMessage(buildMessage(client, builder))

    /**
     * Deletes the original reply message
     */
    suspend fun deleteOriginalMessage() = client.buildRestAction<Unit> {
        route = Route.Interaction.DELETE_ORIGINAL(applicationId, token).delete()
    }

    /**
     * Sends a follow-up message
     */
    suspend fun sendFollowUpMessage(message: DataMessage, ephemeral: Boolean = false) =
        client.buildRestAction<Message> {
            route = Route.Interaction.CREATE_FOLLOW_UP(applicationId, token).post(message.build(ephemeral))
            transform { Message(it.toJsonObject(), client) }
        }

    /**
     * Sends a follow-up message
     */
    suspend fun sendFollowUpMessage(ephemeral: Boolean = false, message: MessageBuilder.() -> Unit) =
        sendFollowUpMessage(buildMessage(client, message), ephemeral)

    /**
     * Sends a follow-up message
     */
    suspend fun sendFollowUpMessage(message: String, ephemeral: Boolean = false) =
        sendFollowUpMessage(ephemeral) { content = message }

    /**
     * Edits a follow-up message
     */
    suspend fun editFollowUpMessage(id: Snowflake, message: DataMessage) = client.buildRestAction<Message> {
        route = Route.Interaction.EDIT_FOLLOW_UP(applicationId, token, id).patch(message.build())
        transform { Message(it.toJsonObject(), client) }
    }

    /**
     * Retrieves a follow-up message
     */
    suspend fun getFollowUpMessage(id: Snowflake) = client.buildRestAction<Message> {
        route = Route.Interaction.GET_FOLLOW_UP(applicationId, token, id).get()
        transform { Message(it.toJsonObject(), client) }
    }

    /**
     * Deletes a follow-up message
     */
    suspend fun deleteFollowUpMessage(id: Snowflake) = client.buildRestAction<Unit> {
        route = Route.Interaction.DELETE_FOLLOW_UP(applicationId, token, id).delete()
    }

}