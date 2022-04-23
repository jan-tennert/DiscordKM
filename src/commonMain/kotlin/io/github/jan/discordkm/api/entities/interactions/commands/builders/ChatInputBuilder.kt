/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions.commands.builders

import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.github.jan.discordkm.api.entities.clients.on
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

class ChatInputCommandBuilder(client: WSDiscordClient? = null) : ApplicationCommandBuilder(ApplicationCommandType.CHAT_INPUT, client) {

    private val options: MutableList<CommandOption> = mutableListOf()

    @CommandBuilder
    inline fun onCommand(crossinline action: suspend SlashCommandEvent.() -> Unit) {
        client?.let { c -> c.on<SlashCommandEvent>(predicate = { it.commandName == name }) { action(this) } }
    }

    @CommandBuilder
    inline fun <T : AutoCompleteEvent<*>> onAutoComplete(
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
    fun options(optionBuilder: SlashCommandOptionBuilder.() -> Unit) {
        val builder = SlashCommandOptionBuilder(translationManager, name)
        builder.optionBuilder()
        options.addAll(builder.options)
    }

    override fun build() = buildJsonObject {
        put("options", JsonArray(options.map(Json::encodeToJsonElement)))
        putJsonObject(super.build())
    }

}

inline fun chatInputCommand(client: WSDiscordClient? = null, builder: ChatInputCommandBuilder.() -> Unit) : ApplicationCommandBuilder {
    val commandBuilder = ChatInputCommandBuilder(client)
    commandBuilder.builder()
    return commandBuilder
}
