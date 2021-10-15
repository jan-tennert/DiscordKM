/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
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