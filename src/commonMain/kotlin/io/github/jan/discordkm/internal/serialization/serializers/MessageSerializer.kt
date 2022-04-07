package io.github.jan.discordkm.internal.serialization.serializers

import com.soywiz.klock.DateTimeTz
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.channels.Channel
import io.github.jan.discordkm.api.entities.channels.ChannelType
import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.role.Role
import io.github.jan.discordkm.api.entities.guild.sticker.Sticker
import io.github.jan.discordkm.api.entities.guild.sticker.StickerItem
import io.github.jan.discordkm.api.entities.interactions.InteractionType
import io.github.jan.discordkm.api.entities.interactions.components.ActionRow
import io.github.jan.discordkm.api.entities.interactions.components.Button
import io.github.jan.discordkm.api.entities.interactions.components.ComponentType
import io.github.jan.discordkm.api.entities.interactions.components.SelectionMenu
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.entities.messages.MessageCacheEntry
import io.github.jan.discordkm.api.entities.messages.MessageCacheEntryImpl
import io.github.jan.discordkm.api.entities.messages.MessageType
import io.github.jan.discordkm.api.entities.messages.componentJson
import io.github.jan.discordkm.internal.serialization.BaseEntitySerializer
import io.github.jan.discordkm.internal.utils.boolean
import io.github.jan.discordkm.internal.utils.get
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.isoTimestamp
import io.github.jan.discordkm.internal.utils.long
import io.github.jan.discordkm.internal.utils.snowflake
import io.github.jan.discordkm.internal.utils.string
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

object MessageSerializer : BaseEntitySerializer<MessageCacheEntry> {
    override fun deserialize(data: JsonObject, value: Client): MessageCacheEntry {
        val guild = data["guild_id", true]?.snowflake?.let { Guild(it, value) }
        val author = data["author"]?.jsonObject?.let { User(it, value) }
        return MessageCacheEntryImpl(
            id = data["id"]!!.snowflake,
            channel = MessageChannel(data["channel_id"]!!.snowflake, value),
            author = author,
            content = data["content"]?.string ?: "",
            timestamp = data["timestamp"]?.isoTimestamp ?: DateTimeTz.nowLocal(),
            editedTimestamp = data["edited_timestamp", true]?.isoTimestamp,
            isTTSMessage = data["tts"]?.boolean ?: false,
            mentions = data["mentions"]?.jsonArray?.map { User(it.jsonObject, value) } ?: emptyList(),
            mentionsEveryone = data["mention_everyone"]?.boolean ?: false,
            attachments = data["attachments"]?.jsonArray?.map { Json.decodeFromJsonElement(it.jsonObject) } ?: emptyList(),
            embeds = data["embeds"]?.jsonArray?.map { Json.decodeFromJsonElement(it.jsonObject) } ?: emptyList(),
            nonce = data["nonce", true]?.string,
            isPinned = data["pinned"]?.boolean ?: false,
            webhookId = data["webhook_id", true]?.snowflake,
            type = data["type"]?.int?.let { MessageType[it] } ?: MessageType.DEFAULT,
            activity = data["activity"]?.let { Json.decodeFromJsonElement(it.jsonObject) },
            reference = data["message_reference"]?.let { Json.decodeFromJsonElement(it.jsonObject) },
            flags = Message.Flag.decode(data["flags", true]?.long ?: 0),
            guild = guild,
            member = author?.id?.let { guild?.cache?.members?.get(it) },
            components = data["components"]?.let { json ->
                json.jsonArray.map { row ->
                    val internalComponents = row.jsonObject["components"]!!.jsonArray.map { component ->
                        when (ComponentType[component.jsonObject["type"]!!.int]) {
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
            thread = data["thread"]?.jsonObject?.let { Thread(it, guild!!) },
            mentionedChannels = data["mention_channels", true]?.jsonArray?.let { mentionedChannels ->
                mentionedChannels.map { deserializeChannelMention(it.jsonObject, guild!!) }
            } ?: emptyList(),
            stickers = data["sticker_items", true]?.jsonArray?.let { stickers ->
                stickers.map { sticker ->
                    StickerItem(sticker.jsonObject["name"]!!.string, sticker.jsonObject["id"]!!.snowflake, Sticker.FormatType[sticker.jsonObject["format_type"]!!.int])
                }
            } ?: emptyList(),
            mentionedRoles = data["mention_roles", true]?.jsonArray?.let { mentionedRoles ->
                mentionedRoles.map { Role(it.jsonObject, guild!!) }
            } ?: emptyList(),
            referencedMessage = data["referenced_message", true]?.let { deserialize(it.jsonObject, value) },
        ).apply {
            data["reactions"]?.jsonArray?.let { reactions ->
                cacheManager.reactionCache.putAll(reactions.map { Json.decodeFromJsonElement(it.jsonObject) })
            }
        }
    }

    private fun deserializeMessageInteraction(data: JsonObject, client: Client) = Message.MessageInteraction(
        data["id"]!!.snowflake,
        InteractionType[data["type"]!!.int],
        data["name"]!!.string,
        User(data["user"]!!.jsonObject, client),
    )

    private fun deserializeChannelMention(data: JsonObject, guild: Guild) = Channel(
        data["id"]!!.snowflake,
        ChannelType[data["type"]!!.int],
        guild.client,
        guild
    )

}