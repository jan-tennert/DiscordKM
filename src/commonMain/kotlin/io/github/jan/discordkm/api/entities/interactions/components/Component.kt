package io.github.jan.discordkm.api.entities.interactions.components

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.interactions.Interaction
import io.github.jan.discordkm.api.entities.messages.DataMessage
import io.github.jan.discordkm.api.entities.messages.MessageBuilder
import io.github.jan.discordkm.internal.restaction.RestAction
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.putJsonObject
import io.github.jan.discordkm.internal.utils.toJsonObject
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