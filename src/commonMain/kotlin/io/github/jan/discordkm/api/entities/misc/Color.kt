/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.misc

import kotlinx.serialization.Serializable

@Serializable
data class Color(val value: Int) {

    companion object {

        fun fromRGB(r: Int, g: Int, b: Int, a: Int = 255) : Color {
            val value = a and 0xFF shl 24 or
                    (r and 0xFF shl 16) or
                    (g and 0xFF shl 8) or
                    (b and 0xFF shl 0)
            return Color(value)
        }

    }

    fun toRGB() = Triple(value shr 16 and 0xFF, value shr 8 and 0xFF, value shr 0 and 0xFF)

}


