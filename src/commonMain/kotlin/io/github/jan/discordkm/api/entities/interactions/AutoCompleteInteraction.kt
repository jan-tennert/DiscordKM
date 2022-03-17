/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.interactions.commands.builders.SlashCommandOptionBuilder
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.put
import io.github.jan.discordkm.internal.utils.toJsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AutoCompleteInteraction<T>(override val client: Client, override val data: JsonObject)  : Interaction {

    /**
     * Replies to the [AutoCompleteInteraction] with the given choices.
     */
    suspend fun replyChoices(choices: SlashCommandOptionBuilder.ChoicesBuilder<T>.() -> Unit) = client.buildRestAction<Unit> {
        val formattedChoices = SlashCommandOptionBuilder.ChoicesBuilder<T>()
            .apply(choices)
            .map { buildJsonObject { put("name", it.name); put("value", it.string) } }
        route = Route.Interaction.CALLBACK(id, token).post(buildJsonObject {
            put("type", InteractionCallbackType.APPLICATION_COMMAND_AUTOCOMPLETE_RESULT)
            put("data", buildJsonObject {
                put("choices", formattedChoices.toJsonArray())
            })
        })
    }

    /**
     * Replies to the [AutoCompleteInteraction] with the given choices.
     */
    suspend fun replyChoices(choices: List<Pair<String, T>>) = replyChoices { choices.forEach { c -> choice(c.first, c.second) } }

    /**
     * Replies to the [AutoCompleteInteraction] with the given choices.
     */
    suspend fun replyChoices(vararg choices: Pair<String, T>) = replyChoices(choices.toList())

}