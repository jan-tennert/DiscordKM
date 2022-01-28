package io.github.jan.discordkm.internal.utils

import arrow.core.firstOrNone
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

interface EnumWithValue<V> {

    val value: V

}

open class EnumWithValueGetter <V : EnumWithValue<T>, T>(val values: Collection<V>) : KSerializer<V> {

    constructor(values: Array<V>) : this(values.toList())

    operator fun get(value: T) = values.first { it.value == value }

    fun getOption(value: T) = values.firstOrNone { it.value == value }

    override val descriptor = PrimitiveSerialDescriptor("EnumWithValue", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): V {
        val value = decoder.decodeInt()
        return values.first { it.value == value }
    }

    override fun serialize(encoder: Encoder, value: V) {
        when(value.value) {
            is String -> encoder.encodeString(value.value as String)
            is Int -> encoder.encodeInt(value.value as Int)
            is Long -> encoder.encodeLong(value.value as Long)
            is Float -> encoder.encodeFloat(value.value as Float)
            is Double -> encoder.encodeDouble(value.value as Double)
            is Boolean -> encoder.encodeBoolean(value.value as Boolean)
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

}