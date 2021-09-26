package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.messages.DataMessage
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.entities.messages.MessageBuilder
import io.github.jan.discordkm.api.entities.messages.buildMessage
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.get
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

open class StandardInteraction(client: Client, data: JsonObject) : Interaction(client, data) {

    /**
     * Replies to this interaction without a message
     */
    suspend fun deferReply(ephemeral: Boolean = false) = client.buildRestAction<Unit> {
        route = Route.Interaction.CALLBACK(id, token).post(buildJsonObject {
            put("type", 5) //reply without message
            if(ephemeral) {
                put("data", buildJsonObject {
                    put("flags", 1 shl 6)
                })
            }
        })
        transform {  }
        onFinish { isAcknowledged = true }
    }

    /**
     * Replies to this interaction
     */
    suspend fun reply(message: DataMessage, ephemeral: Boolean = false) = client.buildRestAction<Unit> {
        route = Route.Interaction.CALLBACK(id, token).post(message.buildCallback(4, ephemeral))
        transform { }
        onFinish { isAcknowledged = true }
    }

    /**
     * Replies to this interaction
     */
    suspend fun reply(ephemeral: Boolean = false, message: MessageBuilder.() -> Unit) = reply(buildMessage(message), ephemeral)

    /**
     * Replies to this interaction
     */
    suspend fun reply(message: String, ephemeral: Boolean = false) = reply(buildMessage { content = message }, ephemeral)

    /**
     * Edits the original reply message
     */
    suspend fun editOriginalMessage(message: DataMessage) = client.buildRestAction<Message> {
        route = Route.Interaction.EDIT_ORIGINAL(applicationId, token).patch(message.build())
        transform { Message(channel!!, it.toJsonObject()) }
    }

    suspend fun editOriginalMessage(builder: MessageBuilder.() -> Unit) = editOriginalMessage(MessageBuilder().apply(builder).build())

    /**
     * Deletes the original reply message
     */
    suspend fun deleteOriginalMessage() = client.buildRestAction<Unit> {
        route = Route.Interaction.DELETE_ORIGINAL(applicationId, token).delete()
        transform {  }
    }

    /**
     * Sends a follow-up message
     */
    suspend fun sendFollowUpMessage(message: DataMessage, ephemeral: Boolean = false) = client.buildRestAction<Message> {
        route = Route.Interaction.CREATE_FOLLOW_UP(applicationId, token).post(message.build(ephemeral))
        transform { Message(channel!!, it.toJsonObject()) }
    }

    /**
     * Sends a follow-up message
     */
    suspend fun sendFollowUpMessage(ephemeral: Boolean = false, message: MessageBuilder.() -> Unit) = sendFollowUpMessage(buildMessage(message), ephemeral)

    /**
     * Sends a follow-up message
     */
    suspend fun sendFollowUpMessage(message: String, ephemeral: Boolean = false) = sendFollowUpMessage(ephemeral) { content = message }

    /**
     * Edits a follow-up message
     */
    suspend fun editFollowUpMessage(id: Snowflake, message: DataMessage) = client.buildRestAction<Message> {
        route = Route.Interaction.EDIT_FOLLOW_UP(applicationId, token, id).patch(message.build())
        transform { Message(channel!!, it.toJsonObject()) }
    }

    /**
     * Retrieves a follow-up message
     */
    suspend fun getFollowUpMessage(id: Snowflake) = client.buildRestAction<Message> {
        route = Route.Interaction.GET_FOLLOW_UP(applicationId, token, id).get()
        transform { Message(channel!!, it.toJsonObject()) }
    }

    /**
     * Deletes a follow-up message
     */
    suspend fun deleteFollowUpMessage(id: Snowflake) = client.buildRestAction<Unit> {
        route = Route.Interaction.DELETE_FOLLOW_UP(applicationId, token, id).delete()
        transform { }
    }

}