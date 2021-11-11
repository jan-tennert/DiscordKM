package io.github.jan.discordkm.api.entities.channels

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Permission
import io.github.jan.discordkm.api.entities.lists.MessageList
import io.github.jan.discordkm.api.entities.messages.DataMessage
import io.github.jan.discordkm.api.entities.messages.EmbedBuilder
import io.github.jan.discordkm.api.entities.messages.Message
import io.github.jan.discordkm.api.entities.messages.MessageBuilder
import io.github.jan.discordkm.api.entities.messages.MessageEmbed
import io.github.jan.discordkm.api.entities.messages.buildEmbed
import io.github.jan.discordkm.api.entities.messages.buildMessage
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.caching.MessageCacheManager
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.toIsoMap
import io.github.jan.discordkm.internal.utils.toJsonObject

interface MessageChannel : Channel {

    suspend fun send(message: DataMessage) = client.buildRestAction<Message> {
        route = Route.Message.CREATE_MESSAGE(id).post(message.build())
        transform { Message(this@MessageChannel, it.toJsonObject()) }
    }

    suspend fun send(builder: MessageBuilder.() -> Unit) = send(buildMessage(builder))

    suspend fun send(content: String) = send(buildMessage { this.content = content })

    suspend fun sendEmbed(embed: EmbedBuilder.() -> Unit) = send(buildMessage { embeds += buildEmbed(embed) })

    suspend fun sendEmbeds(embeds: Iterable<MessageEmbed>) = send { this.embeds.addAll(embeds) }

    /**
     * Starts typing in this channel. This lasts for approximately 10 seconds
     */
    suspend fun sendTyping() = client.buildRestAction<Unit> {
        route = Route.Channel.START_TYPING(id).post()
    }

}

interface MessageChannelCacheEntry: MessageChannel, ChannelCacheEntry{

    val cacheManager: MessageCacheManager
    val lastMessage: Message?

    override suspend fun send(message: DataMessage) = super.send(message).also { cacheManager.messageCache[it.id] = it }

}