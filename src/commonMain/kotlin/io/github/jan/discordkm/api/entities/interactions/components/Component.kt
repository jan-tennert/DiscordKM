package io.github.jan.discordkm.api.entities.interactions.components

import io.github.jan.discordkm.internal.utils.valueOfIndex
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

interface Component {

    val type: ComponentType

}

@Serializable(with = ComponentType.Serializer::class)
enum class ComponentType {
    ACTION_ROW,
    BUTTON,
    SELECTION_MENU;

    object Serializer : KSerializer<ComponentType> {

        override fun deserialize(decoder: Decoder) = valueOfIndex<ComponentType>(decoder.decodeInt(), 1)

        override val descriptor = PrimitiveSerialDescriptor("ComponentType", PrimitiveKind.INT)

        override fun serialize(encoder: Encoder, value: ComponentType) {
            encoder.encodeInt(value.ordinal + 1)
        }

    }
}