/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.messages

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.ISO8601
import com.soywiz.klock.parse
import io.github.jan.discordkm.api.entities.EnumSerializer
import io.github.jan.discordkm.api.entities.Reference
import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.SerializableEnum
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.guild.Sticker
import io.github.jan.discordkm.api.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.api.entities.guild.channels.GuildTextChannel
import io.github.jan.discordkm.api.entities.guild.channels.NewsChannel
import io.github.jan.discordkm.api.entities.guild.channels.Thread
import io.github.jan.discordkm.api.entities.interactions.Interaction
import io.github.jan.discordkm.api.entities.interactions.components.ActionRow
import io.github.jan.discordkm.api.entities.interactions.components.Button
import io.github.jan.discordkm.api.entities.interactions.components.ComponentType
import io.github.jan.discordkm.api.entities.interactions.components.SelectionMenu
import io.github.jan.discordkm.api.entities.lists.ReactionList
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.entities.UserData
import io.github.jan.discordkm.internal.entities.channels.ChannelType
import io.github.jan.discordkm.internal.entities.channels.MessageChannel
import io.github.jan.discordkm.internal.entities.channels.PrivateChannel
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.exceptions.PermissionException
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.put
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.extractClientEntity
import io.github.jan.discordkm.internal.utils.extractGuildEntity
import io.github.jan.discordkm.internal.utils.extractMessageChannelEntity
import io.github.jan.discordkm.internal.utils.getEnums
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.toJsonObject
import io.github.jan.discordkm.internal.utils.valueOfIndex
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

class Message(val channel: MessageChannel, override val data: JsonObject) : SnowflakeEntity, Reference<Message>, SerializableEntity {

    override val id = data.getId()

    /**
     * The id of the channel where this message was sent
     */
    val channelId = data.getOrThrow<Snowflake>("channel_id")

    /**
     * The attachments this message contains
     */
    val attachments = data["attachments"]?.jsonArray?.map { Json.decodeFromJsonElement<MessageAttachment>(it.jsonObject) } ?: emptyList()

    /**
     * The action rows this message contains
     */
    val actionRows = data["components"]?.let { json ->
        json.jsonArray.map { row ->
            val internalComponents = row.jsonObject.getValue("components").jsonArray.map { component ->
                when (valueOfIndex<ComponentType>(component.jsonObject.getOrThrow("type"))) {
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
    } ?: emptyList()

    /**
     * The channel this message was sent to
     */
    override val client = channel.client

    /**
     * Returns a [Guild] if sent in a guild channel
     */
    val guild = client.guilds[data.getOrNull<Snowflake>("guild_id") ?: Snowflake.empty()]

    /**
     * Returns the [UserData] who sent the message
     */
    val author: User
        get() = data["author"]?.jsonObject?.extractClientEntity<User>(client) ?: throw IllegalArgumentException("The message doesn't have an author")

    val member
        get() = guild?.members?.get(author.id)

    /**
     * Returns the content of the message
     */
    val content: String
        get() = data.getOrNull<String>("content") ?: throw IllegalStateException("This message doesn't have any content")

    /**
     * Returns the time the message was sent
     */
    val messageSentTime: DateTimeTz
        get() = ISO8601.DATETIME_UTC_COMPLETE.parse(data.getOrThrow("timestamp"))

    /**
     * Returns the time the message was edited
     */
    val messageEditedTime = ISO8601.DATETIME_UTC_COMPLETE.tryParse(data.getOrNull<String>("edited_timestamp") ?: "")

    /**
     * Whether this message is a text to speech message or not
     */
    @get:JvmName("isTTS")
    val isTTS: Boolean
        get() = data.getOrThrow<Boolean>("tts")

    /**
     * Whether this message mentions everyone or not
     */
    @get:JvmName("mentionsEveryone")
    val mentionsEveryone: Boolean
        get() = data.getOrThrow<Boolean>("mention_everyone")

    /**
     * Returns a list of mentioned users
     */
    val mentionedUsers: List<User>
        get() = data.getValue("mentions").jsonArray.map { it.jsonObject.extractClientEntity(client) }

    /**
     * Returns a list of mentioned roles
     */
    val mentionedRoles: List<Role> = data["mentioned_roles"]?.let {
        it.jsonArray.map { it.jsonObject.extractGuildEntity(guild!!) }
    } ?: emptyList()

    //mentioned channels
    /**
     * Returns a list of embeds included in this message
     */
    val embeds: List<MessageEmbed>
        get() = data.getValue("embeds").jsonArray.map { Json { classDiscriminator = "#class" }.decodeFromJsonElement<MessageEmbed>(it.jsonObject) }

    /**
     * This can be used to validate a message
     */
    val nonce
        get() = data.getOrNull<String>("nonce")

    /**
     * If the message is pinned in the [channel]
     */
    @get:JvmName("isPinned")
    val isPinned: Boolean
        get() = data.getOrThrow<Boolean>("pinned")

    val referencedMessage: Message?
        get() = data["referenced_message"]?.let { Message(channel, it.jsonObject) }

    /**
     * If the message was sent by a webhook this is the id
     */
    val webhookId: Long?
        get() = data.getOrNull<Long>("webhook_id")

    /**
     * The [Type] of the message
     */
    val type = MessageType.values().firstOrNull { it.id == data.getOrNull<Int>("type") } ?: MessageType.DEFAULT

    /**
     * If the message was a response to an interaction, this is the id of the interaction's application
     */
    val applicationId = data.getOrNull<Long>("application_id")

    /**
     * "Data showing the source of a crosspost, channel follow add, pin, or reply message"
     */
    val reference = if(data["message_reference"]?.jsonObject != null) Json.decodeFromString<Reference>(data.getValue("message_reference").jsonObject.toString()) else null

    /**
     * The [Flag]s of the message
     */
    val flags = data.getEnums("flags", Flag)

    /**
     * The [MessageInteraction] object of this message, if this message was sent by an interaction
     */
    val interaction = data["interaction"]?.let { MessageInteraction(this@Message, it.jsonObject) }

    /**
     * Returns the [Sticker.Item]s containing the message
     */
    val stickerItems = data["sticker_items"]?.jsonArray?.map { Sticker.Item(it.jsonObject) } ?: emptyList()

    /**
     * The reaction list
     */
    val reactions = ReactionList(this)

    /**
     * Crossposts this message if it was sent in a [NewsChannel]
     */
    suspend fun crosspost() = client.buildRestAction<Unit> {
        route = Route.Message.CROSSPOST_MESSAGE(channelId.toString(), id).post()
        transform {  }
        check {
            when {
             // TODO: check permissions
                channel.type == ChannelType.DM -> throw UnsupportedOperationException("You can't crosspost a message in a private channel")
                channel.type != ChannelType.GUILD_NEWS -> throw UnsupportedOperationException("You can only crosspost in a news channel!")
            }
        }
    }

    /**
     * Deletes the message in the channel
     * Needs [Permission.MANAGE_MESSAGES] to delete other's messages
     */
    suspend fun delete() = client.buildRestAction<Unit> {
        route = Route.Message.DELETE_MESSAGE(channelId.toString(), id).delete()
        transform {  }
        check {
            if(channel.type == ChannelType.DM && author.id != client.selfUser.id) throw PermissionException("You can't delete others messages in a private channel")
            val canDelete = author.id == client.selfUser.id || Permission.MANAGE_MESSAGES in guild!!.selfMember.getPermissionsFor(channel as GuildChannel)
            if(!canDelete) throw PermissionException("You can't delete others messages without the permission MANAGE_MESSAGES")
        }
    }


    /**
     * Edits this message
     */
    suspend fun edit(message: DataMessage) = client.buildRestAction<Message> {
        route = Route.Message.EDIT_MESSAGE(channel.id, id).patch(Json.encodeToString(message))
        transform { it.toJsonObject().extractMessageChannelEntity(channel) }
        check {
            if(author.id != client.selfUser.id) throw UnsupportedOperationException("You can't edit other's messages!")
        }
    }

    /**
     * Edits this message
     */
    suspend fun edit(message: MessageBuilder.() -> Unit) = edit(buildMessage(message))
    /**
     * Edits this message
     */
    suspend fun edit(content: String) = edit(buildMessage { this.content = content })

    override fun getValue(ref: Any?, property: KProperty<*>) = channel.messages[id]!!

    /**
     * Creates a thread from this message
     * @param name The name this thread will get
     * @param autoArchiveDuration The [Thread.ThreadDuration] after the thread will be achieved
     */

    suspend fun createThread(name: String, autoArchiveDuration: Thread.ThreadDuration = (channel as GuildTextChannel).defaultAutoArchiveDuration) = client.buildRestAction<Thread> {
        route = Route.Thread.START_THREAD_WITH_MESSAGE(channel.id, id).post(buildJsonObject {
            put("name", name)
            put("auto_archive_duration", autoArchiveDuration.duration.minutes.toInt())
        })
        transform { it.toJsonObject().extractGuildEntity(guild!!) }
        onFinish { (guild!! as GuildData).threadCache[it.id] = it }
        check { if(guild == null) throw UnsupportedOperationException("You can't create a thread from a private channel message") }
    }

    /**
     * Pins this message in this channel
     */

    suspend fun pin() = client.buildRestAction<Unit> {
        route = Route.Message.PIN_MESSAGE(channel.id, id).put()
        transform {  }
        check { if(channel is PrivateChannel) throw UnsupportedOperationException("You can't pin a message in a private channel!"); if(isPinned) throw IllegalStateException("You can't pin a pinned message!'")}
    }

    /**
     * Unpins this message in this channel
     */
    suspend fun unpin() = client.buildRestAction<Unit> {
        route = Route.Message.UNPIN_MESSAGE(channel.id, id).delete()
        transform {  }
        check { if(channel is PrivateChannel) throw UnsupportedOperationException("You can't unpin a message in a private channel!"); if(!isPinned) throw IllegalStateException("You can't unpin an unpinned message!") }
    }

    fun copy() = DataMessage(
        content,
        isTTS,
        embeds,
        allowedMentions = AllowedMentions(),
        actionRows = actionRows,
        reference = reference,
        stickerIds = stickerItems.map { it.id }
    )

    class MessageInteraction(val message: Message, override val data: JsonObject) : SerializableEntity {

        override val client: Client
            get() = message.client

        val interactionId = data.getOrThrow<Snowflake>("id")
        val type = valueOfIndex<Interaction.InteractionType>(data.getOrThrow("type"), 1)
        val commandName = data.getOrThrow<String>("name")
        val user: User = UserData(client, data.getValue("user").jsonObject)

    }

    @Serializable
    data class Reference(@SerialName("message_id") val messageId: Long? = null, @SerialName("guild_id") val guildId: Long? = null, @SerialName("channel_id") val channelId: Long? = null, @get:JvmName("failIfNotExists") @SerialName("fail_if_not_exists") val failIfNotExists: Boolean = true)

    enum class Flag(override val offset: Int) : SerializableEnum<Flag> {

        CROSSPOSTED(0),
        IS_CROSSPOST(1),
        SUPPRESS_EMBEDS(2),
        SOURCE_MESSAGE_DELETED(3),
        URGENT(4),
        HAS_THREAD(5),
        EPHEMERAL(6),
        LOADING(7);

        companion object : EnumSerializer<Flag> {
            override val values = values().toList()
        }

    }

    override suspend fun retrieve() = channel.messages.retrieve(id)

}

enum class MessageType(val id: Int) {
    DEFAULT(0),
    RECIPIENT_ADD(1),
    RECIPIENT_REMOVE(2),
    CALL(3),
    CHANNEL_NAME_CHANGE(4),
    CHANNEL_ICON_CHANGE(5),
    CHANNEL_PINNED_MESSAGE(6),
    GUILD_MEMBER_JOIN(7),
    USER_PREMIUM_GUILD_SUBSCRIPTION(8),
    USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_1(9),
    USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_2(10),
    USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_3(11),
    CHANNEL_FOLLOW_ADD(12),
    GUILD_DISCOVERY_DISQUALIFIED(14),
    GUILD_DISCOVERY_REQUALIFIED(15),
    GUILD_DISCOVERY_GRACE_PERIOD_INITIAL_WARNING(16),
    GUILD_DISCOVERY_GRACE_PERIOD_FINAL_WARNING(17),
    THREAD_CREATED(18),
    REPLY(19),
    CHAT_INPUT_COMMAND(20),
    THREAD_STARTER_MESSAGE(21),
    GUILD_INVITE_REMINDER(22),
    CONTEXT_MENU_COMMAND(23)
}

fun buildMessage(builder: MessageBuilder.() -> Unit) = MessageBuilder().apply(builder).build()