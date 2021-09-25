package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.messages.DataMessage
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.entities.messages.MessageBuilder
import io.github.jan.discordkm.api.entities.messages.buildMessage
import io.github.jan.discordkm.internal.restaction.RestAction
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.putJsonObject
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

open class StandardInteraction(client: Client, data: JsonObject) : Interaction(client, data) {

    /**
     * Replies to this interaction without a message
     */
    suspend fun deferReply(ephemeral: Boolean = false) = client.buildRestAction<Unit> {
        action = RestAction.post("/interactions/$id/$token/callback", buildJsonObject {
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
    suspend fun reply(ephemeral: Boolean = false, message: DataMessage) = client.buildRestAction<Unit> {
        action = RestAction.post("/interactions/$id/$token/callback", buildJsonObject {
            put("type", 4) //reply with message
            put("data", buildJsonObject {
                putJsonObject(message.toString().toJsonObject())
                if(ephemeral) put("flags", 1 shl 6)
            })
        })
        transform { }
        onFinish { isAcknowledged = true }
    }

    /**
     * Replies to this interaction
     */
    suspend fun reply(ephemeral: Boolean = false, message: MessageBuilder.() -> Unit) = reply(ephemeral, buildMessage(message))

    /**
     * Replies to this interaction
     */
    suspend fun reply(ephemeral: Boolean = false, message: String) = reply(ephemeral, buildMessage { content = message })

    /**
     * Edits the original reply message
     */
    suspend fun editOriginalMessage(message: DataMessage) = client.buildRestAction<Message> {
        action = RestAction.patch("/webhooks/${applicationId}/$token/messages/@original", message.toString())
        transform { Message(channel!!, it.toJsonObject()) }
    }

    /**
     * Deletes the original reply message
     */
    suspend fun deleteOriginalMessage() = client.buildRestAction<Unit> {
        action = RestAction.delete("/webhooks/$applicationId/$token/messages/@original")
        transform {  }
    }

    /**
     * Sends a follow-up message
     */
    suspend fun sendFollowUpMessage(message: DataMessage) = client.buildRestAction<Message> {
        action = RestAction.post("/webhooks/$applicationId/$token", message.toString())
        transform { Message(channel!!, it.toJsonObject()) }
    }

    /**
     * Sends a follow-up message
     */
    suspend fun sendFollowUpMessage(message: MessageBuilder.() -> Unit) = sendFollowUpMessage(buildMessage(message))

    /**
     * Sends a follow-up message
     */
    suspend fun sendFollowUpMessage(message: String) = sendFollowUpMessage { content = message }

    /**
     * Edits a follow-up message
     */
    suspend fun editFollowUpMessage(id: Snowflake, message: DataMessage) = client.buildRestAction<Message> {
        action = RestAction.patch("/webhooks/$applicationId/$token/messages/$id", message.toString())
        transform { Message(channel!!, it.toJsonObject()) }
    }

    /**
     * Retrieves a follow-up message
     */
    suspend fun getFollowUpMessage(id: Snowflake) = client.buildRestAction<Message> {
        action = RestAction.get("/webhooks/$applicationId/$token/messages/$id")
        transform { Message(channel!!, it.toJsonObject()) }
    }

    /**
     * Deletes a follow-up message
     */
    suspend fun deleteFollowUpMessage(id: Snowflake) = client.buildRestAction<Unit> {
        action = RestAction.delete("/webhooks/$applicationId/$token/messages/$id")
        transform { }
    }

}