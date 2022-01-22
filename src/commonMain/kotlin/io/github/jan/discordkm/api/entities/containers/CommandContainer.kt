package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.interactions.CommandHolder
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommand
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommandType
import io.github.jan.discordkm.api.entities.interactions.commands.builders.ApplicationCommandBuilder
import io.github.jan.discordkm.api.entities.interactions.commands.builders.ChatInputCommandBuilder
import io.github.jan.discordkm.api.entities.interactions.commands.builders.chatInputCommand
import io.github.jan.discordkm.api.entities.interactions.commands.builders.messageCommand
import io.github.jan.discordkm.api.entities.interactions.commands.builders.userCommand
import io.github.jan.discordkm.internal.restaction.RestAction
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.toJsonArray
import io.github.jan.discordkm.internal.utils.toJsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject

open class CommandContainer(private val holder: CommandHolder, private val baseURL: String) {

    /**
     * Creates a new application command
     */
    suspend fun create(builder: ApplicationCommandBuilder) = holder.client.buildRestAction<ApplicationCommand> {
        route = RestAction.post(baseURL, builder.build())
        transform { ApplicationCommand(holder.client, it.toJsonObject()) }
    }

    /**
     * Creates a new chat input command
     */
    suspend fun createChatInputCommand(builder: ChatInputCommandBuilder.() -> Unit) = create(chatInputCommand(holder.client as? DiscordWebSocketClient, builder))

    /**
     * Creates a new message command
     */
    suspend fun createMessageCommand(builder: ApplicationCommandBuilder.() -> Unit) = create(messageCommand(holder.client as? DiscordWebSocketClient, builder))

    /**
     * Creates a new user command
     */
    suspend fun createUserCommand(builder: ApplicationCommandBuilder.() -> Unit) = create(userCommand(holder.client as? DiscordWebSocketClient, builder))

    /**
     * Retrieves all application commands
     */
    suspend fun retrieve() = holder.client.buildRestAction<List<ApplicationCommand>> {
        route = RestAction.get(baseURL)
        transform { it.toJsonArray().map { json -> ApplicationCommand(holder.client, json.jsonObject) } }
    }

    /**
     * Retrieves a specific application command
     */
    suspend fun retrieve(id: Snowflake) = holder.client.buildRestAction<ApplicationCommand> {
        route = RestAction.get("$baseURL/$id")
        transform { ApplicationCommand(holder.client, it.toJsonObject()) }
    }

    /**
     * Modifies an application command
     */
    suspend fun modify(id: Snowflake, builder: ApplicationCommandBuilder) = holder.client.buildRestAction<ApplicationCommand> {
        route = RestAction.patch("$baseURL/$id", builder.build())
        transform { ApplicationCommand(holder.client, it.toJsonObject()) }
    }

    /**
     * Deletes an application command
     */
    suspend fun delete(id: Snowflake) = holder.client.buildRestAction<Unit> {
        route = RestAction.delete("$baseURL/$id")
    }

    /**
     * Overrides all application commands with new ones
     */
    suspend fun override(commands: CommandBulkOverride.() -> Unit) = holder.client.buildRestAction<List<ApplicationCommand>> {
        route = RestAction.put(baseURL, JsonArray(CommandBulkOverride().apply(commands).commands.map(ApplicationCommandBuilder::build)))
        transform { it.toJsonArray().map { json -> ApplicationCommand(holder.client, json.jsonObject) } }
    }

}

class CommandBulkOverride {

    internal val commands = mutableListOf<ApplicationCommandBuilder>()

    fun add(command: ApplicationCommandBuilder) { commands += command }

    operator fun plus(command: ApplicationCommandBuilder) = add(command)

    fun chatInput(builder: ApplicationCommandBuilder.() -> Unit) { add(ApplicationCommandBuilder(
        ApplicationCommandType.CHAT_INPUT, "", "").apply(builder)) }

    fun user(builder: ApplicationCommandBuilder.() -> Unit) { add(ApplicationCommandBuilder(
        ApplicationCommandType.USER, "", "").apply(builder)) }

    fun message(builder: ApplicationCommandBuilder.() -> Unit) { add(ApplicationCommandBuilder(
        ApplicationCommandType.MESSAGE, "", "").apply(builder)) }

}