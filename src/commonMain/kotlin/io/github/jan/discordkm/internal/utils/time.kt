/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.utils

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import com.soywiz.klock.minutes
import com.soywiz.klock.parse
import io.github.jan.discordkm.api.entities.channels.guild.Thread
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

object UnixDateTimeSerializer : KSerializer<DateTimeTz> {

    override fun deserialize(decoder: Decoder) = DateTimeTz.fromUnixLocal(decoder.decodeLong())

    override val descriptor = PrimitiveSerialDescriptor("ISO8601 Timestamp", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: DateTimeTz) = encoder.encodeLong(value.local.unixMillisLong)

}

object ThreadDurationSerializer : KSerializer<Thread.ThreadDuration> {

    override fun deserialize(decoder: Decoder) = Thread.ThreadDuration.raw(decoder.decodeInt().minutes)

    override val descriptor = PrimitiveSerialDescriptor("ThreadDuration", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Thread.ThreadDuration) = encoder.encodeInt(value.duration.minutes.toInt())

}