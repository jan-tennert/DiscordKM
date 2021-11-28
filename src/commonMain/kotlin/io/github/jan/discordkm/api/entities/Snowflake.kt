/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities

import com.soywiz.klock.DateTimeTz
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@Serializable(with = SnowflakeSerializer::class)
@JvmInline
value class Snowflake internal constructor(val long: Long) {

    val string: String
        get() = long.toString()
    val timestamp: DateTimeTz
        get() = DateTimeTz.fromUnixLocal((long shr 22) + 1420070400000)

    companion object {

        fun fromId(id: Long) = Snowflake(id)
        fun fromId(id: String) = Snowflake(id.toLong())
        fun empty() = Snowflake(0L)

    }

    override fun toString() = string

}

object SnowflakeSerializer : KSerializer<Snowflake> {
    override fun deserialize(decoder: Decoder) = Snowflake.fromId(decoder.decodeString())

    override val descriptor = PrimitiveSerialDescriptor("Snowflake", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Snowflake) {
        encoder.encodeString(value.string)
    }

}

/**
 * Objects like [Member], [Role] or [Guild] are Snowflake entities,
 * but if you want a snowflake entity without an object use [fromSnowflake]
 */
interface SnowflakeEntity {

    val id: Snowflake
    val creationDate: DateTimeTz
        get() = id.timestamp

}

//just channel.send and messagelist for retrieve etc.

val Long.asSnowflake: Snowflake
    get() = Snowflake.fromId(this)

val String.asSnowflake: Snowflake
    get() = Snowflake.fromId(this)