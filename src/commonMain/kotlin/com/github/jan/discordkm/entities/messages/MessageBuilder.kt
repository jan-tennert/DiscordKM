package com.github.jan.discordkm.entities.messages

import com.github.jan.discordkm.entities.guild.Sticker
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
    var stickerIds = mutableListOf<Long>()
    var reference: Message.Reference? = null
    var allowedMentions = AllowedMentions()
    var tts = false

    fun sticker(id: Long) { stickerIds += id }

    fun sticker(sticker: Sticker) = sticker(sticker.id)

    fun embed(builder: EmbedBuilder.() -> Unit) { embeds += buildEmbed(builder) }

    fun reference(messageId: Long) {
        reference = Message.Reference(messageId = messageId)
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