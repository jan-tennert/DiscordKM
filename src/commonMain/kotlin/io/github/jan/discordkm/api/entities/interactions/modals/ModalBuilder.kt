package io.github.jan.discordkm.api.entities.interactions.modals

import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.interactions.components.RowLayoutBuilder
import io.github.jan.discordkm.api.events.ModalSubmitEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ModalBuilder(@SerialName("custom_id") val customId: String = "", var title: String = "", @Transient private val client: DiscordWebSocketClient? = null) : RowLayoutBuilder<ModalLayout>() {

    fun onSubmit(callback: suspend ModalSubmitEvent.() -> Unit) {
        client?.on<ModalSubmitEvent>(predicate = { it.modalId == customId }, callback)
    }

}