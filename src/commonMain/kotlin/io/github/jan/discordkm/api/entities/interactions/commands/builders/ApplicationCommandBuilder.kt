/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions.commands.builders

import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.interactions.commands.ApplicationCommandType
import io.github.jan.discordkm.api.entities.interactions.commands.CommandBuilder
import io.github.jan.discordkm.api.entities.interactions.commands.CommandOption
import io.github.jan.discordkm.api.entities.interactions.commands.OptionChoice
import io.github.jan.discordkm.api.events.CommandEvent
import io.github.jan.discordkm.api.events.SlashCommandEvent
import io.github.jan.discordkm.internal.entities.channels.ChannelType
import io.github.jan.discordkm.internal.utils.putJsonObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

open class ApplicationCommandBuilder(val type: ApplicationCommandType, var name: String, var description: String) {

    inline fun <reified E : CommandEvent> onAction(
        client: DiscordWebSocketClient,
        crossinline action: suspend E.() -> Unit
    ) {
        client.on<E>(predicate = { it.commandName == name }) { action(this) }
    }

    internal open fun build() = buildJsonObject {
        put("name", name)
        put("description", description)
        put("type", type.ordinal + 1)
    }

}

class ChatInputCommandBuilder(name: String, description: String, private val options: MutableList<CommandOption>) : ApplicationCommandBuilder(ApplicationCommandType.CHAT_INPUT, name, description) {

    inline fun onAction(
        client: DiscordWebSocketClient,
        subCommand: String? = null,
        subCommandGroup: String? = null,
        crossinline action: SlashCommandAction
    ) {
        client.on<SlashCommandEvent>(predicate = { it.commandName == name && it.subCommand == subCommand && it.subCommandGroup == subCommandGroup }) {
            action(this)
        }
    }

    @CommandBuilder
    fun options(optionBuilder: OptionBuilder.() -> Unit) {
        val builder = OptionBuilder()
        builder.optionBuilder()
        options.addAll(builder.options)
    }

    override fun build() = buildJsonObject {
        put("options", Json.encodeToJsonElement(options))
        putJsonObject(super.build())
    }

}

typealias SlashCommandAction = suspend SlashCommandEvent.() -> Unit

open class OptionBuilder(open val options: MutableList<CommandOption> = mutableListOf()) {

    @CommandBuilder
    fun string(name: String, description: String, required: Boolean = false, autocomplete: Boolean? = null, choices: ChoicesBuilder<String>.() -> Unit = {}) {
        val choiceBuilder = ChoicesBuilder<String>()
        choiceBuilder.choices()
        options += CommandOption(CommandOption.OptionType.STRING, name, description, required, choiceBuilder.choices, autocomplete = autocomplete)
    }

    @CommandBuilder
    fun int(name: String, description: String, required: Boolean = false, choices: ChoicesBuilder<Int>.() -> Unit = {}) {
        val choiceBuilder = ChoicesBuilder<Int>()
        choiceBuilder.choices()
        options += CommandOption(CommandOption.OptionType.INTEGER, name, description, required, choiceBuilder.choices)
    }

    @CommandBuilder
    fun double(name: String, description: String, required: Boolean = false, choices: ChoicesBuilder<Double>.() -> Unit = {}) {
        val choiceBuilder = ChoicesBuilder<Double>()
        choiceBuilder.choices()
        options += CommandOption(CommandOption.OptionType.NUMBER, name, description, required, choiceBuilder.choices)
    }

    @CommandBuilder
    fun boolean(name: String, description: String, required: Boolean = false) { options += CommandOption(CommandOption.OptionType.BOOLEAN, name, description, required) }

    @CommandBuilder
    fun user(name: String, description: String, required: Boolean = false) { options += CommandOption(CommandOption.OptionType.USER, name, description, required) }

    @CommandBuilder
    fun mentionable(name: String, description: String, required: Boolean = false) { options += CommandOption(CommandOption.OptionType.MENTIONABLE, name, description, required) }

    @CommandBuilder
    fun channel(name: String, description: String, required: Boolean = false, types: List<ChannelType> = emptyList()) { options += CommandOption(CommandOption.OptionType.CHANNEL, name, description, required, channelTypes = types) }

    @CommandBuilder
    fun role(name: String, description: String, required: Boolean = false) { options += CommandOption(CommandOption.OptionType.ROLE, name, description, required) }

    @CommandBuilder
    fun subCommand(name: String, description: String, builder: OptionBuilder.() -> Unit = {}) {
        val options = OptionBuilder()
        options.builder()
        this.options += CommandOption(CommandOption.OptionType.SUB_COMMAND, name, description, false, options = options.options)
    }

    @CommandBuilder
    fun subCommandGroup(name: String, description: String, builder: SubCommandGroupBuilder.() -> Unit) {
        val subCommandGroups = SubCommandGroupBuilder()
        subCommandGroups.builder()
        options += CommandOption(CommandOption.OptionType.SUB_COMMAND_GROUP, name, description, false, options = subCommandGroups.subCommands)
    }

    inner class SubCommandGroupBuilder(internal val subCommands: MutableList<CommandOption> = mutableListOf()) {

        @CommandBuilder
        fun subCommand(
            name: String,
            description: String,
            required: Boolean = false,
            builder: OptionBuilder.() -> Unit = {}
        ) {
            val options = OptionBuilder()
            options.builder()
            subCommands += CommandOption(CommandOption.OptionType.SUB_COMMAND, name, description, required, options = options.options)
        }

    }

    class ChoicesBuilder<T>(val choices: MutableList<OptionChoice> = mutableListOf()) {

        fun choice(name: String, value: T) {
            val primitiveValue = when(value) {
                is Int -> JsonPrimitive(value)
                is Double -> JsonPrimitive(value)
                is String -> JsonPrimitive(value)
                else -> throw IllegalStateException()
            }
            choices += OptionChoice(name, primitiveValue)
        }

    }

}

inline fun chatInputCommand(builder: ChatInputCommandBuilder.() -> Unit) : ApplicationCommandBuilder {
    val commandBuilder = ChatInputCommandBuilder("", "", mutableListOf())
    commandBuilder.builder()
    return commandBuilder
}

inline fun messageCommand(builder: ApplicationCommandBuilder.() -> Unit) : ApplicationCommandBuilder {
    val commandBuilder = ApplicationCommandBuilder(ApplicationCommandType.MESSAGE, "", "")
    commandBuilder.builder()
    return commandBuilder
}

inline fun userCommand(builder: ApplicationCommandBuilder.() -> Unit) : ApplicationCommandBuilder {
    val commandBuilder = ApplicationCommandBuilder(ApplicationCommandType.USER, "", "")
    commandBuilder.builder()
    return commandBuilder
}