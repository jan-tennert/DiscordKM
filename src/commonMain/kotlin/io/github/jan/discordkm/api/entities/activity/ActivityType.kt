package io.github.jan.discordkm.api.entities.activity

import io.github.jan.discordkm.internal.utils.valueOfIndex
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ActivityTypeSerializer::class)
enum class ActivityType {
    PLAYING,
    STREAMING,
    LISTENING,
    WATCHING,
    CUSTOM,
    COMPETING
}

object ActivityTypeSerializer : KSerializer<ActivityType> {

    override val descriptor = PrimitiveSerialDescriptor("ActivityType", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder) = valueOfIndex<ActivityType>(decoder.decodeInt())

    override fun serialize(encoder: Encoder, value: ActivityType) {
        encoder.encodeInt(value.ordinal)
    }

}