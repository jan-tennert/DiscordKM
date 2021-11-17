package io.github.jan.discordkm.internal.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

interface EnumWithValue<V> {

    val value: V

}

open class EnumWithValueGetter <V : EnumWithValue<T>, T>(val values: Collection<V>) : KSerializer<V> {

    constructor(values: Array<V>) : this(values.toList())

    operator fun get(value: T) = values.first { it.value == value }

    override val descriptor = PrimitiveSerialDescriptor("EnumWithValue", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder) = decoder.decodeInt().let { values.first { it.value == it } }

    override fun serialize(encoder: Encoder, value: V) {
        when(value.value) {
            is String -> encoder.encodeString(value.value as String)
            is Int -> encoder.encodeInt(value.value as Int)
        }
    }

}