package io.github.jan.discordkm.internal.utils

import com.github.ajalt.colormath.Color
import com.github.ajalt.colormath.model.RGBInt
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ColorSerializer : KSerializer<Color> {

    override val descriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.INT)
    override fun deserialize(decoder: Decoder) = RGBInt.fromRGBA(decoder.decodeInt().toUInt())
    override fun serialize(encoder: Encoder, value: Color) = encoder.encodeInt(value.toSRGB().toRGBInt().argb.toInt())

}