/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.messages

import com.soywiz.klock.ISO8601
import com.soywiz.klock.parse
import io.github.jan.discordkm.entities.EnumSerializer
import io.github.jan.discordkm.entities.Reference
import io.github.jan.discordkm.entities.SerializableEntity
import io.github.jan.discordkm.entities.SerializableEnum
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.SnowflakeEntity
import io.github.jan.discordkm.entities.User
import io.github.jan.discordkm.entities.channels.ChannelType
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.entities.guild.Permission
import io.github.jan.discordkm.entities.guild.Role
import io.github.jan.discordkm.entities.guild.Sticker
import io.github.jan.discordkm.entities.guild.channels.GuildChannel
import io.github.jan.discordkm.entities.guild.channels.NewsChannel
import io.github.jan.discordkm.exceptions.PermissionException
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.extract
import io.github.jan.discordkm.utils.extractClientEntity
import io.github.jan.discordkm.utils.extractGuildEntity
import io.github.jan.discordkm.utils.extractMessageChannelEntity
import io.github.jan.discordkm.utils.getEnums
import io.github.jan.discordkm.utils.getId
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

class Message(val channel: MessageChannel, override val data: JsonObject) : SnowflakeEntity, Reference<Message>, SerializableEntity {

    override val id = data.getId()

    val channelId = data.getOrNull<Snowflake>("channel_id")

    /**
     * The channel this message was sent to
     */
    override val client = channel.client

    /**
     * Returns a [Guild] if sent in a guild channel
     */
    val guild = client.guilds[data.getOrNull<Snowflake>("guild_id") ?: Snowflake.empty()]

    /**
     * Returns the [User] who sent the message
     */
    val author = data.getValue("author").jsonObject.extractClientEntity<User>(client)

    val member = guild?.members?.get(author.id)

    /**
     * Returns the content of the message
     */
    val content = data.getOrThrow<String>("content")

    /**
     * Returns the time the message was sent
     */
    val messageSentTime = ISO8601.DATETIME_UTC_COMPLETE.parse(data.getOrThrow("timestamp"))

    /**
     * Returns the time the message was edited
     */
    val messageEditedTime = ISO8601.DATETIME_UTC_COMPLETE.tryParse(data.getOrNull<String>("edited_timestamp") ?: "")

    /**
     * Whether this message is a text to speech message or not
     */
    @get:JvmName("isTTS")
    val isTTS = data.getOrThrow<Boolean>("tts")

    /**
     * Whether this message mentions everyone or not
     */
    @get:JvmName("mentionsEveryone")
    val mentionsEveryone = data.getOrThrow<Boolean>("mention_everyone")

    /**
     * Returns a list of mentioned users
     */
    val mentionedUsers = data.getValue("mentions").jsonArray.map { it.jsonObject.extractClientEntity<User>(client) }

    /**
     * Returns a list of mentioned roles
     */
    val mentionedRoles: List<Role> = data["mentioned_roles"]?.let {
        it.jsonArray.map { it.jsonObject.extractGuildEntity(guild!!) }
    } ?: emptyList()

    //mentioned channels
    //attachments
    /**
     * Returns a list of embeds included in this message
     */
    val embeds = data.getValue("embeds").jsonArray.map { it.jsonObject.extract<MessageEmbed>() }

    //reactions

    /**
     * This can be used to validate a message
     */
    val nonce = data.getOrNull<String>("nonce")

    /**
     * If the message is pinned in the [channel]
     */
    @get:JvmName("isPinned")
    val isPinned = data.getOrThrow<Boolean>("pinned")

    /**
     * If the message was sent by a webhook this is the id
     */
    val webhookId = data.getOrNull<Long>("webhook_id")

    /**
     * The [Type] of the message
     */
    val type = ChannelType.values().first { it.ordinal == data.getOrThrow<Int>("type") }

    //activity
    //application
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

    //referenced_messages?
    //interaction
    //thread
    //components

    /**
     * Returns the [Sticker.Item]s containing the message
     */
    val stickerItems = data["sticker_items"]?.jsonArray?.map { Sticker.Item(it.jsonObject) } ?: emptyList()

    /**
     * Crossposts this message if it was sent in a [NewsChannel]
     */
    suspend fun crosspost() = client.buildRestAction<Unit> {
        action = RestAction.Action.post("/channels/${channelId}/messages/${id}/crosspost", "")
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
        action = RestAction.Action.delete("/channels/${channelId}/messages/$id")
        transform {  }
        check {
            if(channel.type == ChannelType.DM && author.id != client.selfUser.id) throw PermissionException("You can't delete others messages in a private channel")
            val canDelete = author.id == client.selfUser.id || Permission.MANAGE_MESSAGES in guild!!.selfMember.getPermissionsFor(channel as GuildChannel)
            if(!canDelete) throw PermissionException("You can't delete others messages without the permission MANAGE_MESSAGES")
        }
    }

    suspend fun edit(message: DataMessage) = client.buildRestAction<Message> {
        action = RestAction.Action.patch("/channels/${channel.id}/messages/$id", Json.encodeToString(message))
        transform { it.toJsonObject().extractMessageChannelEntity(channel) }
        check {
            if(author.id != client.selfUser.id) throw UnsupportedOperationException("You can't edit other's messages!")
        }
    }

    suspend fun edit(message: MessageBuilder.() -> Unit) = edit(buildMessage(message))

    suspend fun edit(content: String) = edit(buildMessage { this.content = content })

    override fun getValue(ref: Any?, property: KProperty<*>): Message {
        TODO("Not yet implemented")
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


}

enum class MessageType {
    DEFAULT,
    RECIPIENT_ADD,
    RECIPIENT_REMOVE,
    CALL,
    CHANNEL_NAME_CHANGE,
    CHANNEL_ICON_CHANGE,
    CHANNEL_PINNED_MESSAGE,
    GUILD_MEMBER_JOIN,
    USER_PREMIUM_GUILD_SUBSCRIPTION,
    USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_1,
    USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_2,
    USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_3,
    CHANNEL_FOLLOW_ADD,
    GUILD_DISCOVERY_DISQUALIFIED,
    GUILD_DISCOVERY_REQUALIFIED,
    GUILD_DISCOVERY_GRACE_PERIOD_INITIAL_WARNING,
    GUILD_DISCOVERY_GRACE_PERIOD_FINAL_WARNING,
    THREAD_CREATED,
    REPLY,
    CHAT_INPUT_COMMAND,
    THREAD_STARTER_MESSAGE,
    GUILD_INVITE_REMINDER,
    CONTEXT_MENU_COMMAND
}

fun buildMessage(builder: MessageBuilder.() -> Unit) = MessageBuilder().apply(builder).build()