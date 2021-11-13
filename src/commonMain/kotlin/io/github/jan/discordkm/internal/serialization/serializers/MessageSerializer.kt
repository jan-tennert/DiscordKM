package io.github.jan.discordkm.internal.serialization.serializers

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.channels.Channel
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.interactions.Interaction
import io.github.jan.discordkm.api.entities.interactions.components.ActionRow
import io.github.jan.discordkm.api.entities.interactions.components.Button
import io.github.jan.discordkm.api.entities.interactions.components.ComponentType
import io.github.jan.discordkm.api.entities.interactions.components.SelectionMenu
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.entities.messages.MessageCacheEntry
import io.github.jan.discordkm.api.entities.messages.MessageType
import io.github.jan.discordkm.api.entities.messages.componentJson
import io.github.jan.discordkm.internal.serialization.BaseEntitySerializer
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.isoTimestamp
import io.github.jan.discordkm.internal.utils.long
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import io.github.jan.discordkm.internal.utils.valueOfIndex
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

object MessageSerializer : BaseEntitySerializer<MessageCacheEntry> {
    override fun deserialize(data: JsonObject, value: Client): MessageCacheEntry {
        val guild = data["guild_id"]?.snowflake?.let { Guild.from(it, value) }
        return MessageCacheEntry(
            id = data["id"]!!.snowflake,
            channel = MessageChannel.from(data["channel_id"]!!.snowflake, value),
            author = data["author"]!!.let { User.from(it.jsonObject, value) },
            content = data["content"]!!.string,
            timestamp = data["timestamp"]!!.isoTimestamp,
            editedTimestamp = data["edited_timestamp"]?.isoTimestamp,
            isTTSMessage = data["tts"]!!.boolean,
            mentions = data["mentions"]!!.jsonObject.map { User.from(it.value.jsonObject, value) },
            mentionsEveryone = data["mention_everyone"]!!.boolean,
            attachments = data["attachments"]!!.jsonObject.map { Json.decodeFromJsonElement(it.value.jsonObject) },
            embeds = data["embeds"]!!.jsonObject.map { Json.decodeFromJsonElement(it.value.jsonObject) },
            // reactions = data["reactions"]!!.jsonObject.map { Json.decodeFromJsonElement(it.value.jsonObject) },
            nonce = data["nonce"]?.string,
            isPinned = data["pinned"]!!.boolean,
            webhookId = data["webhook_id"]?.snowflake,
            type = MessageType.get(data["type"]!!.int),
            activity = data["activity"]?.let { Json.decodeFromJsonElement(it.jsonObject) },
            // application = data["application"]?.let { Json.decodeFromJsonElement(it.jsonObject) },
            reference = data["message_reference"]?.let { Json.decodeFromJsonElement(it.jsonObject) },
            flags = Message.Flag.decode(data["flags"]!!.long),
            guild = guild,
            member = data["member"]?.let { Member.from(it.jsonObject, guild!!) },
            components = data["components"]?.let { json ->
                json.jsonArray.map { row ->
                    val internalComponents = row.jsonObject["components"]!!.jsonArray.map { component ->
                        when (valueOfIndex<ComponentType>(component.jsonObject.getOrThrow("type"), 1)) {
                            ComponentType.BUTTON -> componentJson.decodeFromJsonElement(
                                Button.serializer(),
                                component
                            )
                            ComponentType.SELECTION_MENU -> componentJson.decodeFromJsonElement(
                                SelectionMenu.serializer(),
                                component
                            )
                            else -> throw IllegalStateException()
                        }
                    }
                    ActionRow(internalComponents)
                }
            } ?: emptyList(),
            interaction = data["interaction"]?.let { deserializeMessageInteraction(it.jsonObject, value)},
            thread = data["thread"]?.jsonObject?.let { Thread.from(it, guild!!) },
            mentionedChannels = data["mention_channels"]?.jsonArray?.let { mentionedChannels ->
                mentionedChannels.map { deserializeChannelMention(it.jsonObject, guild!!) }
            } ?: emptyList(),
            stickers = data["stickers"]?.jsonArray?.let { stickers ->
                stickers.map { sticker ->
                    StickerSerializer.deserialize(sticker.jsonObject, value)
                }
            } ?: emptyList(),
            mentionedRoles = data["mention_roles"]?.jsonArray?.let { mentionedRoles ->
                mentionedRoles.map { Role.from(it.jsonObject, guild!!) }
            } ?: emptyList(),
            referencedMessage = data["referenced_message"]?.let { deserialize(it.jsonObject, value) },
        )
    }

    private fun deserializeMessageInteraction(data: JsonObject, client: Client) = Message.MessageInteraction(
        data["id"]!!.snowflake,
        Interaction.InteractionType.from(data["type"]!!.int),
        data["name"]!!.string,
        User.from(data["user"]!!.jsonObject, client),
    )

    private fun deserializeChannelMention(data: JsonObject, guild: Guild) = Channel.from(
        data["id"]!!.snowflake,
        ChannelType.get(data["type"]!!.int),
        guild.client,
        guild
    )

}