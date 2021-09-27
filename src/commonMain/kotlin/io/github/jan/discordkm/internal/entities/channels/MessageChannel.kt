/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.entities.channels

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import io.github.jan.discordkm.Cache
import io.github.jan.discordkm.api.entities.Snowflake
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
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.toJsonObject


interface MessageChannel : Channel {

    /**
     * The id of the last message sent in this channel
     */
    val lastMessageId: Snowflake?
        get() = data.getOrNull<Snowflake>("last_message_id")

    /**
     * The time a user without the permission [Permission.MANAGE_MESSAGES] or [Permission.MANAGE_CHANNELS] has to wait before sending another messages
     */
    val slowModeTime: TimeSpan?
        get() = data.getOrNull<Int>("rate_limit_per_user")?.seconds

    val messageCache: Cache<Message>

    val messages: MessageList
        get() = MessageList(this, messageCache.values)

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
        transform {  }
    }

   // fun sendFile(file: VfsFile) : RestAction<DataMessage>

}