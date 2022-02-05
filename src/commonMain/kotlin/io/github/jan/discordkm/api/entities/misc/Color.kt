/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.jan.discordkm.api.entities.misc

import com.soywiz.korio.lang.format
import io.github.jan.discordkm.internal.utils.ColorSerializer
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable(with = ColorSerializer::class)
@JvmInline
value class Color(val rgb: Int) {

    val hex: String
        get() {
            val (r, g, b) = extract()
            return "#%02x%02x%02x".format(r, g, b)
        }

    companion object {

        fun fromRGB(r: Int, g: Int, b: Int, a: Int = 255) : Color {
            val value = a and 0xFF shl 24 or
                    (r and 0xFF shl 16) or
                    (g and 0xFF shl 8) or
                    (b and 0xFF shl 0)
            return Color(value)
        }

        fun fromHex(hex: String): Color {
            val hexValue = hex.replace("#", "")
            return when (hexValue.length) {
                6 -> fromRGB(
                hexValue.substring(0, 2).toInt(16),
                hexValue.substring(2, 4).toInt(16),
                hexValue.substring(4, 6).toInt(16)
                )
                8 -> fromRGB(
                hexValue.substring(0, 2).toInt(16),
                hexValue.substring(2, 4).toInt(16),
                hexValue.substring(4, 6).toInt(16),
                hexValue.substring(6, 8).toInt(16))
                else -> throw IllegalArgumentException("Invalid hex string")
            }
        }
    }

    fun extract() = Triple(rgb shr 16 and 0xFF, rgb shr 8 and 0xFF, rgb shr 0 and 0xFF)

}
