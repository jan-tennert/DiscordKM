package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.messages.DataMessage
import io.github.jan.discordkm.api.entities.messages.MessageBuilder
import io.github.jan.discordkm.internal.restaction.RestAction
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.putJsonObject
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ComponentInteraction(client: Client, data: JsonObject) : StandardInteraction(client, data) {

    suspend fun deferEdit() = client.buildRestAction<Unit> {
        action = RestAction.post("/interactions/$id/$token/callback", buildJsonObject {
            put("type", 6) //defer edit
        })
        transform { }
        onFinish { isAcknowledged = true }
    }

    suspend fun edit(message: DataMessage) = client.buildRestAction<Unit> {
        action = RestAction.post("/interactions/$id/$token/callback", buildJsonObject {
            put("type", 7) //edit
            put("data", buildJsonObject {
                putJsonObject(message.build().toString().toJsonObject())
            })
        })
        transform { }
        onFinish { isAcknowledged = true }
    }

    suspend fun edit(message: MessageBuilder.() -> Unit) = client.buildRestAction<Unit> {
        action = RestAction.post("/interactions/$id/$token/callback", buildJsonObject {
            put("type", 7) //edit
            put("data", buildJsonObject {
                putJsonObject(MessageBuilder().apply(message).build().toString().toJsonObject())
            })
        })
        transform { }
        onFinish { isAcknowledged = true }
    }

}