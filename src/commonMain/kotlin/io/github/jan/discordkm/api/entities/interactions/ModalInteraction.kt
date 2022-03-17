package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.interactions.modals.ModalBuilder
import io.github.jan.discordkm.api.entities.messages.componentJson
import io.github.jan.discordkm.internal.Route
import io.github.jan.discordkm.internal.invoke
import io.github.jan.discordkm.internal.post
import io.github.jan.discordkm.internal.restaction.buildRestAction
import io.github.jan.discordkm.internal.utils.put
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

interface ModalInteraction : Interaction {

    /**
     * Replies to this interaction with a modal (form)
     */
    suspend fun replyModal(builder: ModalBuilder.() -> Unit) = client.buildRestAction<Unit> {
        route = Route.Interaction.CALLBACK(id, token).post(buildJsonObject {
            put("type", InteractionCallbackType.MODAL)
            put("data", componentJson.encodeToJsonElement(ModalBuilder(client).apply(builder)))
        })
    }

}