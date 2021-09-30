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
    val message: Message
        get() = Message(channel!!, data.getValue("message").jsonObject)

    suspend fun deferEdit() = client.buildRestAction<Unit> {
        route = Route.Interaction.CALLBACK(id, token).post(buildJsonObject {
            put("type", 6) //defer edit
        })
        transform { }
        onFinish { isAcknowledged = true }
    }

    suspend fun edit(message: DataMessage) = client.buildRestAction<Unit> {
        route = Route.Interaction.CALLBACK(id, token).post(buildJsonObject {
            put("type", 7) //edit
            put("data", message.build().toString().toJsonObject())
        })
        transform { }
        onFinish { isAcknowledged = true }
    }

    suspend fun edit(message: MessageBuilder.() -> Unit) = edit(MessageBuilder().apply(message).build())

}