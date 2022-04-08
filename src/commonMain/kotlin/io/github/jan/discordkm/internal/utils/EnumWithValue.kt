/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
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

    override val descriptor = PrimitiveSerialDescriptor("EnumWithValue", when(values.first().value) {
        is String -> PrimitiveKind.STRING
        is Int -> PrimitiveKind.INT
        is Long -> PrimitiveKind.LONG
        is Float -> PrimitiveKind.FLOAT
        is Double -> PrimitiveKind.DOUBLE
        is Boolean -> PrimitiveKind.BOOLEAN
        else -> throw IllegalStateException("Unsupported type ${values.first().value!!::class.simpleName}")
    })

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