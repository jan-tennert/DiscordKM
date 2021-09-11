package io.github.jan.discordkm.utils

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import com.soywiz.klock.parse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ISO8601Serializer : KSerializer<DateTimeTz> {
    override fun deserialize(decoder: Decoder) = ISO8601.DATETIME_UTC_COMPLETE.parse(decoder.decodeString())

    override val descriptor = PrimitiveSerialDescriptor("ISO8601 Timestamp", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: DateTimeTz) = encoder.encodeString(ISO8601.DATETIME_UTC_COMPLETE.format(value))
}