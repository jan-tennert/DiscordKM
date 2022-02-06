package io.github.jan.discordkm.api.entities.interactions.modals

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.interactions.components.RowLayoutBuilder
import io.github.jan.discordkm.api.events.ModalSubmitEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ModalBuilder(@Transient override val client: Client? = null, @SerialName("custom_id") var customId: String = "", var title: String = "") : RowLayoutBuilder<ModalLayout>(client) {

    fun onSubmit(callback: suspend ModalSubmitEvent.() -> Unit) {
        if(client is DiscordWebSocketClient) {
            client.on(predicate = { it.modalId == customId }, callback)
        }
    }

}