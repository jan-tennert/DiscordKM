/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.messages

import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.guild.Sticker
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class MessageBuilder {

    var content = ""
    var embeds = mutableListOf<MessageEmbed>()
    //components
    //files
    var stickerIds = mutableListOf<Snowflake>()
    var reference: Message.Reference? = null
    var allowedMentions = AllowedMentions()
    var tts = false

    fun sticker(id: Snowflake) { stickerIds += id }

    fun sticker(sticker: Sticker) = sticker(sticker.id)

    fun embed(builder: EmbedBuilder.() -> Unit) { embeds += buildEmbed(builder) }

    fun reference(messageId: Snowflake) {
        reference = Message.Reference(messageId = messageId.long)
    }

    fun reference(message: Message) = reference(message.id)

    fun build() = DataMessage(content, tts, embeds, allowedMentions)

}

@Serializable(with = AllowedMentionSerializer::class)
enum class AllowedMentionType(val key: String) {
    ROLE_MENTIONS("roles"),
    USER_MENTIONS("users"),
    EVERYONE_MENTIONS("everyone");
}

object AllowedMentionSerializer : KSerializer<AllowedMentionType> {
    override fun deserialize(decoder: Decoder) = AllowedMentionType.values().first { it.key == decoder.decodeString() }

    override val descriptor = PrimitiveSerialDescriptor("Allowed Mention Type", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: AllowedMentionType) = encoder.encodeString(value.key)
}

@Serializable
class AllowedMentions(
    @SerialName("parse")
    val types: List<AllowedMentionType> = emptyList(),
    val roles: List<Long> = emptyList(),
    val users: List<Long> = emptyList(),
    @SerialName("replied_user")
    val replyToUser: Boolean = false
)

@Serializable
data class DataMessage @Deprecated("Use buildMessage method") constructor(
    val content: String = "",
    val tts: Boolean = false,
    val embeds: List<MessageEmbed> = emptyList(),
    @SerialName("allowed_mentions") val allowedMentions: AllowedMentions
)
/*
Missing:
file
components
payload_json

 */