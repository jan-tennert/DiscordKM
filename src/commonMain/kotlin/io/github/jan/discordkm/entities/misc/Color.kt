package io.github.jan.discordkm.entities.misc

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


