package io.github.jan.discordkm.entities.interactions.components

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.interactions.Interaction
import io.github.jan.discordkm.entities.messages.DataMessage
import io.github.jan.discordkm.entities.messages.MessageBuilder
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.putJsonObject
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface Component {

    val type: ComponentType
}


class ComponentInteraction(client: Client, data: JsonObject) : Interaction(client, data) {

    suspend fun deferEdit() = client.buildRestAction<Unit> {
        action = RestAction.Action.post("/interactions/$id/$token/callback", buildJsonObject {
            put("type", 6) //defer edit
        })
        transform { }
        onFinish { isAcknowledged = true }
    }

    suspend fun edit(message: DataMessage) = client.buildRestAction<Unit> {
        action = RestAction.Action.post("/interactions/$id/$token/callback", buildJsonObject {
            put("type", 7) //edit
            put("data", buildJsonObject {
                putJsonObject(message.buildJson().toJsonObject())
            })
        })
        transform { }
        onFinish { isAcknowledged = true }
    }

    suspend fun edit(message: MessageBuilder.() -> Unit) = client.buildRestAction<Unit> {
        action = RestAction.Action.post("/interactions/$id/$token/callback", buildJsonObject {
            put("type", 7) //edit
            put("data", buildJsonObject {
                putJsonObject(MessageBuilder().apply(message).build().buildJson().toJsonObject())
            })
        })
        transform { }
        onFinish { isAcknowledged = true }
    }

}

@Serializable(with = ComponentType.Serializer::class)
enum class ComponentType {
    ACTION_ROW,
    BUTTON,
    SELECTION_MENU;

    object Serializer : KSerializer<ComponentType> {
        override fun deserialize(decoder: Decoder): ComponentType {
            val type = decoder.decodeInt()
            return values().first { it.ordinal + 1 == type }
        }

        override val descriptor = PrimitiveSerialDescriptor("ComponentType", PrimitiveKind.INT)

        override fun serialize(encoder: Encoder, value: ComponentType) {
            encoder.encodeInt(value.ordinal + 1)
        }

    }
}