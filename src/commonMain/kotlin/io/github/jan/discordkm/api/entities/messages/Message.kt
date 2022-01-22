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
import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.activity.Activity
import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.channels.guild.GuildChannel
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.containers.CacheReactionContainer
import io.github.jan.discordkm.api.entities.containers.ReactionContainer
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Member
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.guild.Role
import io.github.jan.discordkm.api.entities.guild.Sticker
import io.github.jan.discordkm.api.entities.interactions.InteractionType
import io.github.jan.discordkm.api.entities.interactions.components.ActionRow
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.CacheEntity
import io.github.jan.discordkm.internal.caching.CacheEntry
import io.github.jan.discordkm.internal.caching.ReactionCacheManager
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.put
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.FlagSerializer
import io.github.jan.discordkm.internal.serialization.SerializableEnum
import io.github.jan.discordkm.internal.serialization.serializers.MessageSerializer
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.jvm.JvmName

sealed interface Message : SnowflakeEntity, BaseEntity, CacheEntity {

    override val id: Snowflake
    override val client: Client
        get() = channel.client
    val guild: Guild?
        get() = null
    val channel: MessageChannel
    val reactions: ReactionContainer
        get() = ReactionContainer(this)
    override val cache: MessageCacheEntry?
        get() = channel.cache?.cacheManager?.messageCache?.get(id)

    /**
     * Crossposts this message if it was sent in a [NewsChannel]
     */
    suspend fun crosspost() = client.buildRestAction<Unit> {
        route = Route.Message.CROSSPOST_MESSAGE(channel.id.toString(), id).post()
    }

    /**
     * Deletes the message in the channel
     * Needs [Permission.MANAGE_MESSAGES] to delete other's messages
     * @param reason The reason which will be displayed in the audit logs
     */
    suspend fun delete(reason: String? = null) = client.buildRestAction<Unit> {
        route = Route.Message.DELETE_MESSAGE(channel.id.toString(), id).delete()
        this.reason = reason
    }

    /**
     * Edits this message
     */
    suspend fun edit(message: DataMessage) = client.buildRestAction<MessageCacheEntry> {
        route = Route.Message.EDIT_MESSAGE(channel.id, id).patch(Json.encodeToString(message))
        transform { invoke(it.toJsonObject(), client) }
    }

    /**
     * Edits this message
     */
    suspend fun edit(message: MessageBuilder.() -> Unit) = edit(buildMessage(message))
    /**
     * Edits this message
     */
    suspend fun edit(content: String) = edit(buildMessage { this.content = content })

    /**
     * Replies to this message
     */
    suspend fun reply(message: DataMessage) = channel.send {
        import(message)
        reference(this@Message)
    }

    /**
     * Replies to this message
     */
    suspend fun reply(builder: MessageBuilder.() -> Unit) = reply(buildMessage(builder))

    /**
     * Replies to this message
     */
    suspend fun reply(content: String) = reply { this.content = content }

    /**
     * Creates a thread from this message
     * @param name The name this thread will get
     * @param autoArchiveDuration The [Thread.ThreadDuration] after the thread will be achieved
     * @param slowModeTime The time in seconds after which a user can send another message
     * @param reason The reason which will be displayed in the audit logs
     */
    suspend fun createThread(name: String, autoArchiveDuration: Thread.ThreadDuration = Thread.ThreadDuration.DAY, slowModeTime: TimeSpan? = null, reason: String? = null) = client.buildRestAction<Thread> {
        route = Route.Thread.START_THREAD_WITH_MESSAGE(channel.id, id).post(buildJsonObject {
            put("name", name)
            putOptional("rate_limit_per_user", slowModeTime?.seconds)
            put("auto_archive_duration", autoArchiveDuration.duration.minutes.toInt())
        })
        transform { Thread(it.toJsonObject(), guild!!) }
        this.reason = reason
    }

    /**
     * Pins this message in this channel
     */
    suspend fun pin(reason: String? = null) = client.buildRestAction<Unit> {
        route = Route.Message.PIN_MESSAGE(channel.id, id).put()
        this.reason = reason
    }

    /**
     * Unpins this message in this channel
     */
    suspend fun unpin(reason: String? = null) = client.buildRestAction<Unit> {
        route = Route.Message.UNPIN_MESSAGE(channel.id, id).delete()
        this.reason = reason
    }

    /**
     * The message reference is sent when e.g. a message replies to another message
     */
    @Serializable
    data class Reference(@SerialName("message_id") val messageId: Snowflake? = null, @SerialName("guild_id") val guildId: Snowflake? = null, @SerialName("channel_id") val channelId: Snowflake? = null, @get:JvmName("failIfNotExists") @SerialName("fail_if_not_exists") val failIfNotExists: Boolean = true)

    enum class Flag(override val offset: Int) : SerializableEnum<Flag> {
        CROSSPOSTED(0),
        IS_CROSSPOST(1),
        SUPPRESS_EMBEDS(2),
        SOURCE_MESSAGE_DELETED(3),
        URGENT(4),
        HAS_THREAD(5),
        EPHEMERAL(6),
        LOADING(7);

        companion object : FlagSerializer<Flag>(values())

    }

    /**
     * The message interaction object is sent when it was sent by an interaction
     */
    class MessageInteraction(
        val interactionId: Snowflake,
        val type: InteractionType,
        val commandName: String,
        val user: User
    )

    companion object {
        operator fun invoke(id: Snowflake, channel: MessageChannel): Message = IndependentMessage(id, channel)
        operator fun invoke(data: JsonObject, client: Client) = MessageSerializer.deserialize(data, client)
    }
}

data class IndependentMessage(override val id: Snowflake, override val channel: MessageChannel) : Message

/**
 * Represents a message sent in a [MessageChannel]
 *
 * @param channel The channel this message was sent in
 * @param id The id of this message
 * @param author The author of this message
 * @param content The content of this message
 * @param timestamp The timestamp when this message was sent
 * @param editedTimestamp The timestamp when this message was edited
 * @param isTTSMessage Whether this message was sent with text-to-speech
 * @param mentions The mentioned users
 * @param attachments The attachments of this message
 * @param embeds The embeds of this message
 * @param flags The flags of this message
 * @param nonce The nonce of this message
 * @param isPinned Whether this message is pinned
 * @param activity The activity of this message
 * @param reference The message reference of this message
 * @param interaction The message interaction of this message
 * @param guild The guild this message was sent in
 * @param webhookId The id of the webhook this message was sent by
 * @param type The type of this message
 * @param stickers The stickers of this message
 * @param components The message components of this message
 */
data class MessageCacheEntry(
    override val id: Snowflake,
    override val guild: Guild? = null,
    override val channel: MessageChannel,
    val author: User?,
    val member: Member?,
    val content: String,
    val timestamp: DateTimeTz,
    val editedTimestamp: DateTimeTz?,
    val isTTSMessage: Boolean,
    val mentionsEveryone: Boolean,
    val mentions: List<User>,
    val mentionedRoles: List<Role>,
    val mentionedChannels: List<GuildChannel>,
    val attachments: List<MessageAttachment>,
    val embeds: List<MessageEmbed>,
    val nonce: String?,
    val isPinned: Boolean,
    val type: MessageType,
    val activity: Activity?,
    val flags: Set<Message.Flag>,
    val stickers: List<Sticker>,
    val components: List<ActionRow>,
    val thread: Thread?,
    val interaction: Message.MessageInteraction?,
    val reference: Message.Reference?,
    val referencedMessage: MessageCacheEntry?,
    val webhookId: Snowflake?
) : Message, CacheEntry {

    val cacheManager = ReactionCacheManager(client)

    override val reactions: CacheReactionContainer
        get() = CacheReactionContainer(this, cacheManager.reactionCache.values.toList())

}

enum class MessageType(override val value: Int) : EnumWithValue<Int> {
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
    CONTEXT_MENU_COMMAND(23);

    companion object : EnumWithValueGetter<MessageType, Int>(values())
}

fun buildMessage(builder: MessageBuilder.() -> Unit) = MessageBuilder().apply(builder).build()