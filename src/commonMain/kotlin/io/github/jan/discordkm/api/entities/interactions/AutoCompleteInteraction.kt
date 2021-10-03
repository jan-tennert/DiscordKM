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
import io.github.jan.discordkm.api.entities.interactions.commands.builders.OptionBuilder
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

class AutoCompleteInteraction(client: Client, data: JsonObject) : Interaction(client, data) {

    /**
     * Replies to the [AutoCompleteInteraction] with the given choices.
     */
    suspend fun replyChoices(choices: OptionBuilder.ChoicesBuilder<String>.() -> Unit) = client.buildRestAction<Unit> {
        val formattedChoices = OptionBuilder.ChoicesBuilder<String>().apply(choices).choices.map { buildJsonObject { put("name", it.name); put("value", it.string) } }
        route = Route.Interaction.CALLBACK(id, token).post(buildJsonObject {
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