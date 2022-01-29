package io.github.jan.discordkm.api.entities.interactions.commands.builders

import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommandType
import io.github.jan.discordkm.api.entities.interactions.commands.CommandBuilder
import io.github.jan.discordkm.api.entities.interactions.commands.CommandOption
import io.github.jan.discordkm.api.events.AutoCompleteEvent
import io.github.jan.discordkm.api.events.SlashCommandEvent
import io.github.jan.discordkm.internal.utils.putJsonObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

class ChatInputCommandBuilder(name: String, description: String, private val options: MutableList<CommandOption>, client: DiscordWebSocketClient? = null) : ApplicationCommandBuilder(
    ApplicationCommandType.CHAT_INPUT, name, description, client) {

    @CommandBuilder
    inline fun onCommand(
        subCommand: String? = null,
        subCommandGroup: String? = null,
        crossinline action: suspend SlashCommandEvent.() -> Unit
    ) {
        client?.let { c -> c.on<SlashCommandEvent>(predicate = { it.commandName == name && it.subCommand == subCommand && it.subCommandGroup == subCommandGroup }) {
            action(this)
        } }
    }

    @CommandBuilder
    inline fun <T> onAutoComplete(
        optionName: String,
        subCommand: String? = null,
        subCommandGroup: String? = null,
        crossinline action: suspend AutoCompleteEvent<T>.() -> Unit
    ) {
        client?.let { c -> c.on<AutoCompleteEvent<T>>(predicate = { it.commandName == name && it.subCommand == subCommand && it.subCommandGroup == subCommandGroup && it.optionName == optionName }) {
            action(this)
        } }
    }

    @CommandBuilder
    fun options(optionBuilder: OptionBuilder.() -> Unit) {
        val builder = OptionBuilder()
        builder.optionBuilder()
        options.addAll(builder.options)
    }

    override fun build() = buildJsonObject {
        put("options", JsonArray(options.map(Json::encodeToJsonElement)))
        putJsonObject(super.build())
    }

}

inline fun chatInputCommand(client: DiscordWebSocketClient? = null, builder: ChatInputCommandBuilder.() -> Unit) : ApplicationCommandBuilder {
    val commandBuilder = ChatInputCommandBuilder("", "", mutableListOf(), client)
    commandBuilder.builder()
    return commandBuilder
}