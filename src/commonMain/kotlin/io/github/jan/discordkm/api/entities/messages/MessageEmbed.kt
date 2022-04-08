/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.messages

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.api.entities.misc.Color
import io.github.jan.discordkm.internal.utils.ColorSerializer
import io.github.jan.discordkm.internal.utils.ISO8601Serializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class MessageEmbed(
    val title: String? = null,
    val type: EmbedType = EmbedType.UNKNOWN,
    val description: String? = null,
    val url: String? = null,
    @Serializable(with = ISO8601Serializer::class)
    val timestamp: DateTimeTz? = null,
    @Serializable(with = ColorSerializer::class)
    val color: Color? = null,
    val footer: Footer? = null,
    val image: Media? = null,
    val thumbnail: Media? = null,
    val video: Media? = null,
    val provider: Provider? = null,
    val author: Author? = null,
    val fields: List<Field> = emptyList()
) {

    @Serializable
    data class Author(val name: String? = null, val url: String? = null, @SerialName("icon_url") val iconUrl: String? = null, @SerialName("proxy_icon_url") val proxyIconUrl: String? = null)

    @Serializable
    data class Provider(val url: String? = null, val name: String? = null)

    @Serializable
    data class Media(val url: String = "", @SerialName("proxy_url") val proxyUrl: String? = null, val height: Int = 0, val width: Int = 0)

    @Serializable
    data class Footer(val text: String = "", @SerialName("icon_url") val iconUrl: String? = null, @SerialName("proxy_icon_url") val proxyIconUrl: String? = null)

    @Serializable
    data class Field(val name: String = "", val value: String = "", val inline: Boolean = false)

}

@Serializable(with = EmbedType.EmbedTypeSerializer::class)
enum class EmbedType {
    UNKNOWN,
    RICH,
    IMAGE,
    VIDEO,
    GIFV,
    ARTICLE,
    LINK;

    object EmbedTypeSerializer : KSerializer<EmbedType> {

        override fun deserialize(decoder: Decoder): EmbedType {
            val type = decoder.decodeString()
            return values()
                .first{
                    it.name == type.uppercase()
                }
        }

        override val descriptor = PrimitiveSerialDescriptor("EmbedType", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: EmbedType) {
            encoder.encodeString(value.name.lowercase())
        }

    }
}