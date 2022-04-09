/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.containers

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
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

    /*
     * Creates a new application command
     */
    suspend fun create(builder: ApplicationCommandBuilder) = holder.client.buildRestAction<ApplicationCommand> {
        route = RestAction.post(baseURL, builder.build())
        transform { println(it); ApplicationCommand(holder.client, it.toJsonObject()) }
    }

    /*
     * Creates a new chat input command
     */
    suspend fun createChatInputCommand(builder: ChatInputCommandBuilder.() -> Unit) = create(chatInputCommand(holder.client as? WSDiscordClient, builder))

    /*
     * Creates a new message command
     */
    suspend fun createMessageCommand(builder: ApplicationCommandBuilder.() -> Unit) = create(messageCommand(holder.client as? WSDiscordClient, builder))

    /*
     * Creates a new user command
     */
    suspend fun createUserCommand(builder: ApplicationCommandBuilder.() -> Unit) = create(userCommand(holder.client as? WSDiscordClient, builder))

    /*
     * Retrieves all application commands
     */
    suspend fun retrieveAll() = holder.client.buildRestAction<List<ApplicationCommand>> {
        route = RestAction.get(baseURL)
        transform { it.toJsonArray().map { json -> ApplicationCommand(holder.client, json.jsonObject) } }
    }

    /*
     * Retrieves a specific application command
     */
    suspend fun retrieve(id: Snowflake) = holder.client.buildRestAction<ApplicationCommand> {
        route = RestAction.get("$baseURL/$id")
        transform { ApplicationCommand(holder.client, it.toJsonObject()) }
    }

    /*
     * Modifies an application command
     */
    suspend fun modify(id: Snowflake, builder: ApplicationCommandBuilder) = holder.client.buildRestAction<ApplicationCommand> {
        route = RestAction.patch("$baseURL/$id", builder.build())
        transform { ApplicationCommand(holder.client, it.toJsonObject()) }
    }

    /*
     * Modifies a user command
    */
    suspend fun modifyUserCommand(id: Snowflake, builder: ApplicationCommandBuilder.() -> Unit) = modify(id, ApplicationCommandBuilder(ApplicationCommandType.USER).apply(builder))

    /*
     * Modifies a message command
    */
    suspend fun modifyMessageCommand(id: Snowflake, builder: ApplicationCommandBuilder.() -> Unit) = modify(id, ApplicationCommandBuilder(ApplicationCommandType.MESSAGE).apply(builder))

    /*
     * Modifies a chat input command
    */
    suspend fun modifyChatInputCommand(id: Snowflake, builder: ChatInputCommandBuilder.() -> Unit) = modify(id, chatInputCommand(holder.client as? WSDiscordClient, builder))

    /*
     * Deletes an application command
     */
    suspend fun delete(id: Snowflake) = holder.client.buildRestAction<Unit> {
        route = RestAction.delete("$baseURL/$id")
    }

    /*
     * Overrides all application commands with new ones
     */
    suspend fun override(commands: CommandBulkOverride.() -> Unit) = holder.client.buildRestAction<List<ApplicationCommand>> {
        route = RestAction.put(baseURL, JsonArray(CommandBulkOverride(holder.client as? WSDiscordClient).apply(commands).commands.map(ApplicationCommandBuilder::build)))
        transform { it.toJsonArray().map { json -> ApplicationCommand(holder.client, json.jsonObject) } }
    }

}

class CommandBulkOverride(private val client: WSDiscordClient?) {

    internal val commands = mutableListOf<ApplicationCommandBuilder>()

    fun add(command: ApplicationCommandBuilder) { commands += command }

    operator fun plus(command: ApplicationCommandBuilder) = add(command)

    fun chatInput(builder: ApplicationCommandBuilder.() -> Unit) { add(ApplicationCommandBuilder(
        ApplicationCommandType.CHAT_INPUT, client).apply(builder)) }

    fun user(builder: ApplicationCommandBuilder.() -> Unit) { add(ApplicationCommandBuilder(
        ApplicationCommandType.USER, client).apply(builder)) }

    fun message(builder: ApplicationCommandBuilder.() -> Unit) { add(ApplicationCommandBuilder(
        ApplicationCommandType.MESSAGE, client).apply(builder)) }

}