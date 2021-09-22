/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.entities.interactions

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Mentionable
import io.github.jan.discordkm.entities.SerializableEntity
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.User
import io.github.jan.discordkm.entities.channels.Channel
import io.github.jan.discordkm.entities.channels.MessageChannel
import io.github.jan.discordkm.entities.guild.Member
import io.github.jan.discordkm.entities.guild.Role
import io.github.jan.discordkm.entities.lists.retrieve
import io.github.jan.discordkm.entities.messages.DataMessage
import io.github.jan.discordkm.entities.messages.MessageBuilder
import io.github.jan.discordkm.entities.messages.buildMessage
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.getOrNull
import io.github.jan.discordkm.utils.getOrThrow
import io.github.jan.discordkm.utils.putJsonObject
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

open class Interaction(override val client: Client, override val data: JsonObject) : SerializableEntity {

    val token: String
        get() = data.getOrThrow<String>("token")

    val type: InteractionType
        get() = InteractionType.values().first { it.ordinal == data.getOrThrow<Int>("type") }

    val id: Snowflake
        get() = data.getOrThrow("id")

    val applicationId: Snowflake
        get() = data.getOrThrow("application_id")

    val guildId: Snowflake?
        get() = data.getOrNull("guild_id")

    val member: Member?
        get() = data["member"]?.let { Member(client.guilds[guildId!!]!!, it.jsonObject) }

    val channelId: Snowflake
        get() = data.getOrThrow("channel_id")

    val user: User?
        get() = data["user"]?.let { User(client, it.jsonObject) }

    val channel: MessageChannel?
        get() = client.channels[channelId] as? MessageChannel

    var isAcknowledged: Boolean = false

    suspend fun retrieveGuild() = client.guilds.retrieve(guildId!!)

    suspend fun retrieveChannel() = client.channels.retrieve(channelId)

    suspend fun deferReply(ephemeral: Boolean = false) = client.buildRestAction<Unit> {
        action = RestAction.Action.post("/interactions/$id/$token/callback", buildJsonObject {
            put("type", 5) //reply without message
            if(ephemeral) {
                put("data", buildJsonObject {
                    put("flags", 1 shl 6)
                })
            }
        })
        transform {  }
        onFinish { isAcknowledged = true }
    }

    suspend fun reply(ephemeral: Boolean = false, message: DataMessage) = client.buildRestAction<Unit> {
        action = RestAction.Action.post("/interactions/$id/$token/callback", buildJsonObject {
            put("type", 4) //reply with message
            put("data", buildJsonObject {
                putJsonObject(message.buildJson().toJsonObject())
                if(ephemeral) put("flags", 1 shl 6)
            })
        })
        transform {  }
        onFinish { isAcknowledged = true }
    }

    suspend fun reply(ephemeral: Boolean = false, message: MessageBuilder.() -> Unit) = reply(ephemeral, buildMessage(message))

    class InteractionOption(val name: String, val value: Any) {

        val user: User
            get() = value as User

        val int: Int
            get() = value as Int

        val double: Double
            get() = value as Double

        val channel: Channel
            get() = value as Channel

        val mentionable: Mentionable
            get() = value as Mentionable

        val role: Role
            get() = value as Role

        val string: String
            get() = value.toString()

        val boolean: Boolean
            get() = value as Boolean

    }

    enum class InteractionType {
        PING,
        APPLICATION_COMMAND,
        MESSAGE_COMPONENT
    }

}

