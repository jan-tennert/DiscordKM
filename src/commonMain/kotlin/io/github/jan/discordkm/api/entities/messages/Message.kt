/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.messages

import com.soywiz.klock.TimeSpan
import io.github.jan.discordkm.api.entities.BaseEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.channels.MessageChannel
import io.github.jan.discordkm.api.entities.channels.guild.Thread
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.containers.ReactionContainer
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.interactions.InteractionType
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.CacheEntity
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.put
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.serialization.FlagSerializer
import io.github.jan.discordkm.internal.serialization.SerializableEnum
import io.github.jan.discordkm.internal.serialization.serializers.MessageSerializer
import io.github.jan.discordkm.internal.utils.putOptional
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
    suspend fun edit(overwrite: Boolean = false, message: DataMessage) = client.buildRestAction<MessageCacheEntry> {
        route = Route.Message.EDIT_MESSAGE(channel.id, id).patch(buildMessage {
            if(!overwrite) this@Message.cache?.let { import(it) }
        }.build(enableStickers = false))
        transform { invoke(it.toJsonObject(), client) }
    }

    /**
     * Edits this message
     */
    suspend fun edit(overwrite: Boolean = false, message: MessageBuilder.() -> Unit) = edit(overwrite, buildMessage(client, message))

    /**
     * Edits this message
     */
    suspend fun edit(overwrite: Boolean = false, content: String) = edit(overwrite, buildMessage(client) { this.content = content })

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
    suspend fun reply(builder: MessageBuilder.() -> Unit) = reply(buildMessage(client, builder))

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
        operator fun invoke(id: Snowflake, channel: MessageChannel): Message = MessageImpl(id, channel)
        operator fun invoke(data: JsonObject, client: Client) = MessageSerializer.deserialize(data, client)
    }
}

internal class MessageImpl(override val id: Snowflake, override val channel: MessageChannel) : Message {

    override fun equals(other: Any?) = other is Message && other.id == id
    override fun hashCode() = id.hashCode()
    override fun toString() = "IndependentMessage[id=$id)"

}