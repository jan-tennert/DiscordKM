/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.webhooks

import com.soywiz.klogger.Logger
import com.soywiz.korio.net.http.HttpClient
import com.soywiz.korio.net.http.createHttpClient
import io.github.jan.discordkm.api.entities.SerializableEntity
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.SnowflakeEntity
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.ClientConfig
import io.github.jan.discordkm.api.entities.messages.DataMessage
import io.github.jan.discordkm.api.entities.messages.MessageBuilder
import io.github.jan.discordkm.api.entities.messages.MessageCacheEntry
import io.github.jan.discordkm.api.entities.messages.buildMessage
import io.github.jan.discordkm.api.entities.modifiers.WebhookModifier
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.delete
import io.github.jan.discordkm.internal.entities.DiscordImage
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.patch
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.Requester
import io.github.jan.discordkm.internal.restaction.RestAction
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.EnumWithValue
import io.github.jan.discordkm.internal.utils.EnumWithValueGetter
import io.github.jan.discordkm.internal.utils.LoggerConfig
import io.github.jan.discordkm.internal.utils.getId
import io.github.jan.discordkm.internal.utils.getOrNull
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.int
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonObject

class Webhook(override val client: Client, override val data: JsonObject) : SerializableEntity, WebhookExecutor {

    /**
     * The id of the webhook
     */
    override val id = data.getId()

    /**
     * The [WebhookType] of the webhook
     */
    val type = WebhookType[data["type"]!!.int]

    val guildId = data.getOrNull<Snowflake>("guild_id")

    val channelId = data.getOrNull<Snowflake>("channel_id")

    /**
     * The name of the webhook
     */
    val name = data.getOrThrow<String>("name")

    val avatarUrl = data.getOrNull<String>("avatar")?.let { DiscordImage.userAvatar(id, it) }

    /**
     * The token for the webhook. Can be empty
     */
    override val token = data.getOrNull<String>("token") ?: ""

    val applicationId = data.getOrNull<Snowflake>("application_id")

    override val http: HttpClient
        get() = client.requester.http

    override val requester: Requester
        get() = client.requester

    /**
     * Deletes this webhook
     */
    suspend fun delete() = client.buildRestAction<Unit> {
        route = if(token.isEmpty()) {
            Route.Webhook.DELETE(id).delete()
        } else {
            Route.Webhook.DELETE_WITH_TOKEN(id, token).delete()
        }
        transform {  }
    }

    /**
     * Modifies this webhook
     */
    suspend fun modify(modifier: WebhookModifier.() -> Unit) = client.buildRestAction<Webhook> {
        val data = WebhookModifier().apply(modifier).data
        route = if(token.isEmpty()) {
            Route.Webhook.MODIFY(id).patch(data)
        } else {
            Route.Webhook.MODIFY_WITH_TOKEN(id, token).patch(data)
        }
        transform { Webhook(client, it.toJsonObject()) }
    }

    enum class WebhookType : EnumWithValue<Int> {
        INCOMING,
        CHANNEL_FOLLOWER,
        APPLICATION;

        override val value: Int
            get() = ordinal + 1

        companion object : EnumWithValueGetter<WebhookType, Int>(values())
    }

    companion object {

        val WEBHOOK_PATTERN = "(?:https?://)?(?:\\w+\\.)?discord(?:app)?\\.com/api(?:/v\\d+)?/webhooks/(\\d+)/([\\w-]+)(?:/(?:\\w+)?)?".toRegex()

        fun fromUrl(url: String): WebhookExecutor {
            val groups = WEBHOOK_PATTERN.matchEntire(url)?.groups?.drop(1) ?: throw IllegalArgumentException("Invalid webhook url: $url")
            return invoke(Snowflake(groups[0]!!.value), groups[1]!!.value)
        }

        operator fun invoke(id: Snowflake, token: String) = object : WebhookExecutor {

            override val id = id

            override val token = token

            override val http = createHttpClient()

            override val requester = Requester(ClientConfig("", logging = LoggerConfig(Logger.Level.DEBUG)))

        }

    }

}

interface WebhookExecutor : SnowflakeEntity {

    val token: String
    val requester: Requester
    val http: HttpClient

    suspend fun send(message: DataMessage) = RestAction<MessageCacheEntry>(requester).apply {
        route = Route.Webhook.EXECUTE_WEBHOOK(id, token).post(message.build())
    }

    suspend fun send(message: MessageBuilder.() -> Unit) = send(buildMessage(builder = message))

    suspend fun send(message: String) = send { content = message }
}

