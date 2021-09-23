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
import io.github.jan.discordkm.entities.interactions.commands.CommandOption
import io.github.jan.discordkm.entities.interactions.commands.builders.OptionBuilder
import io.github.jan.discordkm.entities.lists.retrieve
import io.github.jan.discordkm.entities.messages.DataMessage
import io.github.jan.discordkm.entities.messages.Message
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
import kotlinx.serialization.json.putJsonArray

open class Interaction(override val client: Client, override val data: JsonObject) : SerializableEntity {

    /**
     * The interaction token
     */
    val token: String
        get() = data.getOrThrow("token")

    /**
     * The [InteractionType]
     */
    val type: InteractionType
        get() = InteractionType.values().first { it.ordinal == data.getOrThrow<Int>("type") }

    /**
     * The interaction id
     */
    val id: Snowflake
        get() = data.getOrThrow("id")

    /**
     * The application id
     */
    val applicationId: Snowflake
        get() = data.getOrThrow("application_id")

    /**
     * The guild id, if this was sent in a guild
     */
    val guildId: Snowflake?
        get() = data.getOrNull("guild_id")

    /**
     * The member, if a guild member was involved in this interaction
     */
    val member: Member?
        get() = data["member"]?.let { Member(client.guilds[guildId!!]!!, it.jsonObject) }

    /**
     * The channel id, if this interaction was sent in a channel
     */
    val channelId: Snowflake
        get() = data.getOrThrow("channel_id")

    /**
     * The user, if this interaction was sent in a private channel
     */
    val user: User?
        get() = data["user"]?.let { User(client, it.jsonObject) }

    val channel: MessageChannel?
        get() = client.channels[channelId] as? MessageChannel

    /**
     * The message, if this is a ComponentInteraction
     */
    val message: Message?
        get() = data["message"]?.let { Message(channel!!, it.jsonObject) }

    /**
     * Whether this interaction was already acknowledged
     */
    var isAcknowledged: Boolean = false
        internal set

    suspend fun retrieveChannel() = client.channels.retrieve(channelId)

    /**
     * Replies to this interaction without a message
     */
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

    /**
     * Replies to this interaction
     */
    suspend fun reply(ephemeral: Boolean = false, message: DataMessage) = client.buildRestAction<Unit> {
        action = RestAction.Action.post("/interactions/$id/$token/callback", buildJsonObject {
            put("type", 4) //reply with message
            put("data", buildJsonObject {
                putJsonObject(message.buildJson().toJsonObject())
                if(ephemeral) put("flags", 1 shl 6)
            })
        })
        transform { }
        onFinish { isAcknowledged = true }
    }

    /**
     * Replies to this interaction
     */
    suspend fun reply(ephemeral: Boolean = false, message: MessageBuilder.() -> Unit) = reply(ephemeral, buildMessage(message))

    /**
     * Replies to this interaction
     */
    suspend fun reply(ephemeral: Boolean = false, message: String) = reply(ephemeral, buildMessage { content = message })

    /**
     * Edits the original reply message
     */
    suspend fun editOriginalMessage(message: DataMessage) = client.buildRestAction<Message> {
        action = RestAction.Action.patch("/webhooks/${applicationId}/$token/messages/@original", message.buildJson())
        transform { Message(channel!!, it.toJsonObject()) }
    }

    /**
     * Deletes the original reply message
     */
    suspend fun deleteOriginalMessage() = client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/webhooks/$applicationId/$token/messages/@original")
        transform {  }
    }

    /**
     * Sends a follow-up message
     */
    suspend fun sendFollowUpMessage(message: DataMessage) = client.buildRestAction<Message> {
        action = RestAction.Action.post("/webhooks/$applicationId/$token", message.buildJson())
        transform { Message(channel!!, it.toJsonObject()) }
    }

    /**
     * Sends a follow-up message
     */
    suspend fun sendFollowUpMessage(message: MessageBuilder.() -> Unit) = sendFollowUpMessage(buildMessage(message))

    /**
     * Sends a follow-up message
     */
    suspend fun sendFollowUpMessage(message: String) = sendFollowUpMessage { content = message }

    /**
     * Edits a follow-up message
     */
    suspend fun editFollowUpMessage(id: Snowflake, message: DataMessage) = client.buildRestAction<Message> {
        action = RestAction.Action.patch("/webhooks/$applicationId/$token/messages/$id", message.buildJson())
        transform { Message(channel!!, it.toJsonObject()) }
    }

    /**
     * Retrieves a follow-up message
     */
    suspend fun getFollowUpMessage(id: Snowflake) = client.buildRestAction<Message> {
        action = RestAction.Action.get("/webhooks/$applicationId/$token/messages/$id")
        transform { Message(channel!!, it.toJsonObject()) }
    }

    /**
     * Deletes a follow-up message
     */
    suspend fun deleteFollowUpMessage(id: Snowflake) = client.buildRestAction<Unit> {
        action = RestAction.Action.delete("/webhooks/$applicationId/$token/messages/$id")
        transform { }
    }

    class InteractionOption(val name: String, val type: CommandOption.OptionType, val value: Any) {

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
        MESSAGE_COMPONENT,
        APPLICATION_COMMAND_AUTOCOMPLETE
    }

}

class AutoCompleteInteraction(client: Client, data: JsonObject) : Interaction(client, data) {

    /**
     * Replies to the [AutoCompleteInteraction] with the given choices.
     */
    suspend fun replyChoices(choices: OptionBuilder.ChoicesBuilder<String>.() -> Unit) = client.buildRestAction<Unit> {
        val formattedChoices = OptionBuilder.ChoicesBuilder<String>().apply(choices).choices.map { buildJsonObject { put("name", it.name); put("value", it.string) } }
        action = RestAction.Action.post("/interactions/$id/$token/callback", buildJsonObject {
            put("type", 8) //reply choices
            put("data", buildJsonObject {
                putJsonArray("choices") {
                    formattedChoices.forEach { add(it) }
                }
            })
        })
        transform { }
        onFinish { isAcknowledged = true }
    }

}