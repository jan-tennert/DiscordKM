package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.interactions.modals.ModalBuilder
import io.github.jan.discordkm.internal.DiscordKMUnstable
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

interface ModalInteraction : Interaction {

    /**
     * Replies to this interaction with a modal (form)
     */
    @DiscordKMUnstable
    suspend fun replyModal(builder: ModalBuilder.() -> Unit) = client.buildRestAction<Unit> {
        route = Route.Interaction.CALLBACK(id, token).post(buildJsonObject {
            put("type", 9) //reply with modal
            put("data", Json.encodeToJsonElement(ModalBuilder().apply(builder)))
        })
    }

}