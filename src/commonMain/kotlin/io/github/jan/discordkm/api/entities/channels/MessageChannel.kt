package io.github.jan.discordkm.api.entities.channels

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.containers.MessageContainer
import io.github.jan.discordkm.api.entities.messages.DataMessage
import io.github.jan.discordkm.api.entities.messages.EmbedBuilder
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.entities.messages.MessageBuilder
import io.github.jan.discordkm.api.entities.messages.MessageCacheEntry
import io.github.jan.discordkm.api.entities.messages.MessageEmbed
import io.github.jan.discordkm.api.entities.messages.buildEmbed
import io.github.jan.discordkm.api.entities.messages.buildMessage
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.MessageCacheManager
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.toJsonObject

interface MessageChannel : Channel {

    override val cache: MessageChannelCacheEntry?

    /**
     * Deletes a message from this channel.
     * @param reason The reason which will be displayed in the audit logs.
     * @param messageId The ID of the message to delete.
     */
    suspend fun delete(messageId: Snowflake, reason: String? = null) = client.buildRestAction<Unit> {
        route = Route.Message.DELETE_MESSAGE(id, messageId).delete()
        this.reason = reason
    }

    suspend fun send(message: DataMessage) = client.buildRestAction<MessageCacheEntry> {
        route = Route.Message.CREATE_MESSAGE(id).post(message.build())
        transform { Message(it.toJsonObject(), client) }
    }

    suspend fun send(builder: MessageBuilder.() -> Unit) = send(buildMessage(client, builder))

    suspend fun send(content: String) = send(buildMessage(client) { this.content = content })

    suspend fun sendEmbed(embed: EmbedBuilder.() -> Unit) = send(buildMessage(client) { embeds += buildEmbed(embed) })

    suspend fun sendEmbeds(embeds: Iterable<MessageEmbed>) = send { this.embeds.addAll(embeds) }

    /**
     * Starts typing in this channel. This lasts for approximately 10 seconds
     */
    suspend fun sendTyping() = client.buildRestAction<Unit> {
        route = Route.Channel.START_TYPING(id).post()
    }

    companion object {
        operator fun invoke(id: Snowflake, client: Client): MessageChannel = object : MessageChannel {
            override val id: Snowflake = id
            override val client: Client = client
            override val type: ChannelType
                get() = cache?.type ?: ChannelType.UNKNOWN
            override val cache: MessageChannelCacheEntry?
                get() = client.channels[id] as? MessageChannelCacheEntry
        }
    }

}

interface MessageChannelCacheEntry : MessageChannel, ChannelCacheEntry {

    val cacheManager: MessageCacheManager
    val lastMessage: Message?
    val messages: MessageContainer
        get() = MessageContainer(cacheManager.messageCache.values, this)

    override suspend fun send(message: DataMessage) = super.send(message).also { cacheManager.messageCache[it.id] = it }

}