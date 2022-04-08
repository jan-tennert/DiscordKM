/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.channels

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.github.jan.discordkm.api.entities.clients.WSDiscordClientImpl
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

    /*
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

    /*
     * Starts typing in this channel. This lasts for approximately 10 seconds
     */
    suspend fun sendTyping() = client.buildRestAction<Unit> {
        route = Route.Channel.START_TYPING(id).post()
    }

    companion object {
        operator fun invoke(id: Snowflake, client: DiscordClient): MessageChannel = MessageChannelImpl(id, client)
    }

}

internal class MessageChannelImpl(
    override val id: Snowflake,
    override val client: DiscordClient,
    override val type: ChannelType = ChannelType.UNKNOWN
) : MessageChannel {

    override val cache: MessageChannelCacheEntry?
        get() = client.channels[id] as? MessageChannelCacheEntry

    override fun toString(): String = "MessageChannel(id=$id, type=$type)"
    override fun equals(other: Any?): Boolean = other is MessageChannel && other.id == id
    override fun hashCode(): Int = id.hashCode()

}

interface MessageChannelCacheEntry : MessageChannel, ChannelCacheEntry {

    val cacheManager: MessageCacheManager
    val lastMessage: Message?
        get() = if(client is WSDiscordClient) {
            (client as WSDiscordClientImpl).lastMessages[id]
        } else null
    val messages: MessageContainer
        get() = MessageContainer(cacheManager.messageCache.values, this)

    override suspend fun send(message: DataMessage) = super.send(message).also { cacheManager.messageCache[it.id] = it }

}