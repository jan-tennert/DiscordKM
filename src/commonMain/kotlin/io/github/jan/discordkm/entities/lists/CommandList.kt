package io.github.jan.discordkm.entities.lists

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.Snowflake
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.entities.interactions.commands.ApplicationCommand
import io.github.jan.discordkm.entities.interactions.commands.builders.ApplicationCommandBuilder
import io.github.jan.discordkm.entities.interactions.commands.builders.ChatInputCommandBuilder
import io.github.jan.discordkm.entities.interactions.commands.builders.chatInputCommand
import io.github.jan.discordkm.entities.interactions.commands.builders.messageCommand
import io.github.jan.discordkm.entities.interactions.commands.builders.userCommand
import io.github.jan.discordkm.restaction.RestAction
import io.github.jan.discordkm.restaction.buildRestAction
import io.github.jan.discordkm.utils.toJsonArray
import io.github.jan.discordkm.utils.toJsonObject
import kotlinx.serialization.json.jsonObject

open class CommandList(private val baseURL: String, val client: Client, override val internalList: List<ApplicationCommand>) : DiscordList<ApplicationCommand> {

    override fun get(name: String) = internalList.filter { it.name == name }

    suspend fun create(builder: ApplicationCommandBuilder) = client.buildRestAction<ApplicationCommand> {
        println(builder.build())
        action = RestAction.Action.post(baseURL, builder.build())
        transform { ApplicationCommand(client, it.toJsonObject()) }
        //cache
    }

    suspend fun createChatInputCommand(builder: ChatInputCommandBuilder.() -> Unit) = create(chatInputCommand(builder))

    suspend fun createMessageCommand(builder: ApplicationCommandBuilder.() -> Unit) = create(messageCommand(builder))

    suspend fun createUserCommand(builder: ApplicationCommandBuilder.() -> Unit) = create(userCommand(builder))

    suspend fun retrieveCommands() = client.buildRestAction<List<ApplicationCommand>> {
        action = RestAction.Action.get(baseURL)
        transform { it.toJsonArray().map { json -> ApplicationCommand(client, json.jsonObject)} }
        //cache
    }

    suspend fun retrieve(id: Snowflake) = client.buildRestAction<ApplicationCommand> {
        action = RestAction.Action.get("$baseURL/$id")
        transform { ApplicationCommand(client, it.toJsonObject()) }
        //cache
    }

    suspend fun modify(id: Snowflake, builder: ApplicationCommandBuilder) = client.buildRestAction<ApplicationCommand> {
        action = RestAction.Action.patch("$baseURL/$id", builder.build())
        transform { ApplicationCommand(client, it.toJsonObject()) }
        //cache
    }

    suspend fun delete(id: Snowflake) = client.buildRestAction<ApplicationCommand> {
        action = RestAction.Action.delete("$baseURL/$id")
        //cache
    }

    //bulk override etc


}

class GuildCommandList(val guild: Guild, internalList: List<ApplicationCommand>) : CommandList("/applications/${guild.client.selfUser.id}/guilds/${guild.id}/commands", guild.client, internalList)