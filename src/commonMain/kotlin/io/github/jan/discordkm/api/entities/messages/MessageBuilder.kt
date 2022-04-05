/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.messages

import com.soywiz.kds.fastCastTo
import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.Mentionable
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.guild.Sticker
import io.github.jan.discordkm.api.entities.interactions.components.ActionRow
import io.github.jan.discordkm.api.entities.interactions.components.Button
import io.github.jan.discordkm.api.entities.interactions.components.Component
import io.github.jan.discordkm.api.entities.interactions.components.MessageLayout
import io.github.jan.discordkm.api.entities.interactions.components.RowBuilder
import io.github.jan.discordkm.api.entities.interactions.components.RowLayoutBuilder
import io.github.jan.discordkm.api.entities.interactions.components.SelectionMenu
import io.github.jan.discordkm.api.entities.interactions.modals.TextInputBuilder
import io.github.jan.discordkm.api.media.Attachment
import io.github.jan.discordkm.internal.DiscordKMInternal
import io.github.jan.discordkm.internal.utils.putJsonObject
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.FormPart
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.utils.buildHeaders
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class MessageBuilder internal constructor(private val client: Client? = null) {

    var content = ""
    var embeds = mutableListOf<MessageEmbed>()
    var stickerIds = mutableListOf<Snowflake>()
    var reference: Message.Reference? = null
    var allowedMentions = AllowedMentions()
    var tts = false
    var attachments: MutableList<Attachment> = mutableListOf()
    var actionRows = mutableListOf<ActionRow>()
    private val oldAttachments = mutableListOf<MessageAttachment>()

    fun import(message: DataMessage) {
        content = message.content
        embeds = message.embeds.toMutableList()
        reference = message.reference
        stickerIds = message.stickerIds.toMutableList()
        allowedMentions = message.allowedMentions
        tts = message.tts
        attachments = message.attachments.toMutableList()
        actionRows = message.actionRows.toMutableList()
        oldAttachments.addAll(message.oldAttachments)
    }

    fun import(message: MessageCacheEntry) = import(message.fastCastTo<MessageCacheEntryImpl>().copy())

    fun reference(message: Message) { reference = Message.Reference(message.id, message.guild!!.id, message.channel.id, true) }

    fun allowedMentions(builder: AllowedMentions.() -> Unit) { allowedMentions = AllowedMentions().apply(builder) }

    fun actionRow(builder: RowBuilder<MessageLayout>.() -> Unit) { actionRows += RowBuilder<MessageLayout>(client).apply(builder).build() }

    fun actionRows(builder: RowLayoutBuilder<MessageLayout>.() -> Unit) { actionRows += RowLayoutBuilder<MessageLayout>(client).apply(builder).rows }

    fun sticker(id: Snowflake) { stickerIds += id }

    fun sticker(sticker: Sticker) = sticker(sticker.id)

    fun file(attachment: Attachment) { attachments += attachment }

    fun attachment(attachment: MessageAttachment) { oldAttachments += attachment }

    fun embed(builder: EmbedBuilder.() -> Unit) { embeds += buildEmbed(builder) }

    @OptIn(DiscordKMInternal::class)
    fun build() = DataMessage(content, tts, embeds, allowedMentions, attachments, actionRows, reference, stickerIds, oldAttachments)

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
data class AllowedMentions(
    @SerialName("parse")
    val types: Set<AllowedMentionType> = setOf(),
    val roles: MutableList<Snowflake> = mutableListOf(),
    val users: MutableList<Snowflake> = mutableListOf(),
    @SerialName("replied_user")
    val replyToUser: Boolean = false
) {

    fun mention(mentionable: Mentionable) = when(mentionable) {
        is User -> users.add(mentionable.id)
        is Role -> roles.add(mentionable.id)
        else -> throw IllegalArgumentException("Can't mention type ${mentionable::class}")
    }

}

val componentJson = Json {
    classDiscriminator = "classType"
    ignoreUnknownKeys = true
    encodeDefaults = true
    serializersModule = SerializersModule {
        polymorphic(Component::class) {
            subclass(SelectionMenu::class)
            subclass(Button::class)
            subclass(ActionRow::class)
            subclass(TextInputBuilder::class)
        }
    }
}

class DataMessage @DiscordKMInternal constructor(
    val content: String = "",
    val tts: Boolean = false,
    val embeds: List<MessageEmbed> = emptyList(),
    val allowedMentions: AllowedMentions,
    val attachments: List<Attachment> = emptyList(), //ToDo
    val actionRows: List<ActionRow>,
    val reference: Message.Reference? = null,
    val stickerIds: List<Snowflake> = emptyList(),
    val oldAttachments: List<MessageAttachment> = emptyList()
) {

    private fun buildJson(enableStickers: Boolean = true) = buildJsonObject {
        put("content", content)
        put("tts", tts)
        put("embeds", Json.encodeToJsonElement(embeds).jsonArray)
        put("allowed_mentions", Json.encodeToJsonElement(allowedMentions).jsonObject)
        put("components", componentJson.encodeToJsonElement(actionRows))
        put("message_reference", Json.encodeToJsonElement(reference))
        if(enableStickers) put("sticker_ids", Json.encodeToJsonElement(stickerIds))
        put("attachments", Json.encodeToJsonElement(oldAttachments))
    }

    fun build(ephemeral: Boolean = false, enableStickers: Boolean = true): Any = if(attachments.isEmpty()) {
        buildJsonObject {
            putJsonObject(buildJson(enableStickers))
            if(ephemeral) put("flags", 1 shl 6)
        }
    } else {
        MultiPartFormDataContent(
            formData {
                addAttachments(attachments)
                append(FormPart("payload_json", buildJsonObject {
                    putJsonObject(buildJson())
                    if(ephemeral) put("flags", 1 shl 6)
                }.toString(), headers = buildHeaders {
                    append(HttpHeaders.ContentType, "application/json")
                }))
            })
    }

    fun buildCallback(type: Int, ephemeral: Boolean = false): Any = if(attachments.isEmpty()) {
        buildJsonObject {
            put("type", type)
            put("data", buildJsonObject {
                putJsonObject(buildJson())
                if(ephemeral) put("flags", 1 shl 6)
            })
        }
    } else {
        MultiPartFormDataContent(
            formData {
                addAttachments(attachments)
                append(FormPart("payload_json", buildJsonObject {
                    putJsonObject(buildJsonObject {
                        put("type", type)
                        put("data", buildJsonObject {
                            putJsonObject(buildJson())
                            if(ephemeral) put("flags", 1 shl 6)
                        })
                    })
                }.toString(), headers = buildHeaders {
                    append(HttpHeaders.ContentType, "application/json")
                }))
            })
    }

    private fun FormBuilder.addAttachments(attachments: List<Attachment>) {
        var index = 1
        attachments.forEach {
            val name = if(it.spoiler) "SPOILER_${it.fileName}" else it.fileName
            appendInput(
                key = "file$index",
                headers = Headers.build {
                    append(HttpHeaders.ContentDisposition,
                        "form-data; filename=$name")
                },
                size = it.size
            ) { buildPacket { writeFully(it.bytes) } }
            index++
        }
    }

}

fun buildMessage(client: Client? = null, builder: MessageBuilder.() -> Unit) = MessageBuilder(client).apply(builder).build()
fun BaseEntity.buildMessage(builder: MessageBuilder.() -> Unit) = buildMessage(client, builder)