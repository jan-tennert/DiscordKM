package io.github.jan.discordkm.api.entities.lists

import io.github.jan.discordkm.api.entities.Snowflake
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

open class CommandList(private val baseURL: String, val holder: CommandHolder, override val internalList: List<ApplicationCommand>) : DiscordList<ApplicationCommand> {

    override fun get(name: String) = internalList.filter { it.name == name }

    suspend fun create(builder: ApplicationCommandBuilder) = holder.client.buildRestAction<ApplicationCommand> {
        route = RestAction.post(baseURL, builder.build())
        transform { ApplicationCommand(client, it.toJsonObject()) }
        onFinish { holder.commandCache[it.id] = it }
    }

    suspend fun createChatInputCommand(builder: ChatInputCommandBuilder.() -> Unit) = create(chatInputCommand(builder))

    suspend fun createMessageCommand(builder: ApplicationCommandBuilder.() -> Unit) = create(messageCommand(builder))

    suspend fun createUserCommand(builder: ApplicationCommandBuilder.() -> Unit) = create(userCommand(builder))

    suspend fun retrieveCommands() = holder.client.buildRestAction<List<ApplicationCommand>> {
        route = RestAction.get(baseURL)
        transform { it.toJsonArray().map { json -> ApplicationCommand(client, json.jsonObject)} }
        onFinish { holder.commandCache.internalMap.clear(); holder.commandCache.internalMap.putAll(it.associateBy { command -> command.id }) }
    }

    suspend fun retrieve(id: Snowflake) = holder.client.buildRestAction<ApplicationCommand> {
        route = RestAction.get("$baseURL/$id")
        transform { ApplicationCommand(client, it.toJsonObject()) }
        onFinish { holder.commandCache[it.id] = it }
    }

    suspend fun modify(id: Snowflake, builder: ApplicationCommandBuilder) = holder.client.buildRestAction<ApplicationCommand> {
        route = RestAction.patch("$baseURL/$id", builder.build())
        transform { ApplicationCommand(client, it.toJsonObject()) }
        onFinish { holder.commandCache[it.id] = it }
    }

    suspend fun delete(id: Snowflake) = holder.client.buildRestAction<Unit> {
        route = RestAction.delete("$baseURL/$id")
        onFinish { holder.commandCache.remove(id) }
    }

    suspend fun overrideCommands(commands: CommandBulkOverride.() -> Unit) = holder.client.buildRestAction<List<ApplicationCommand>> {
        route = RestAction.put(baseURL, Json.encodeToJsonElement(CommandBulkOverride().apply(commands).commands.map { it.build() }))
        transform { it.toJsonArray().map { json -> ApplicationCommand(client, json.jsonObject)} }
        onFinish { holder.commandCache.internalMap.clear(); holder.commandCache.internalMap.putAll(it.associateBy { command -> command.id }) }
    }


}

class CommandBulkOverride {

    internal val commands = mutableListOf<ApplicationCommandBuilder>()

    fun add(command: ApplicationCommandBuilder) { commands += command }

    operator fun plus(command: ApplicationCommandBuilder) = add(command)

    fun chatInputCommand(builder: ApplicationCommandBuilder.() -> Unit) { add(ApplicationCommandBuilder(ApplicationCommandType.CHAT_INPUT, "", "").apply(builder)) }

    fun userCommand(builder: ApplicationCommandBuilder.() -> Unit) { add(ApplicationCommandBuilder(ApplicationCommandType.USER, "", "").apply(builder)) }

    fun messageCommand(builder: ApplicationCommandBuilder.() -> Unit) { add(ApplicationCommandBuilder(ApplicationCommandType.MESSAGE, "", "").apply(builder)) }

}