package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.interactions.commands.builders.OptionBuilder
import io.github.jan.discordkm.internal.restaction.RestAction
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
        action = RestAction.post("/interactions/$id/$token/callback", buildJsonObject {
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