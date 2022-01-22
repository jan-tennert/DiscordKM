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
import io.github.jan.discordkm.api.events.CommandEvent
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

open class ApplicationCommandBuilder(val type: ApplicationCommandType, var name: String, var description: String, val client: DiscordWebSocketClient? = null) {

    @CommandBuilder
    inline fun <reified E : CommandEvent> onCommand(
        crossinline action: suspend E.() -> Unit
    ) {
        client?.let { c -> c.on<E>(predicate = { it.commandName == name }) { action(this) } }
    }

    internal open fun build() = buildJsonObject {
        put("name", name)
        put("description", description)
        put("type", type.ordinal + 1)
    }

}

inline fun messageCommand(client: DiscordWebSocketClient? = null, builder: ApplicationCommandBuilder.() -> Unit) : ApplicationCommandBuilder {
    val commandBuilder = ApplicationCommandBuilder(ApplicationCommandType.MESSAGE, "", "", client)
    commandBuilder.builder()
    return commandBuilder
}

inline fun userCommand(client: DiscordWebSocketClient? = null, builder: ApplicationCommandBuilder.() -> Unit) : ApplicationCommandBuilder {
    val commandBuilder = ApplicationCommandBuilder(ApplicationCommandType.USER, "", "", client)
    commandBuilder.builder()
    return commandBuilder
}