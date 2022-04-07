package io.github.jan.discordkm.api.entities.interactions.modals

import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.github.jan.discordkm.api.entities.clients.on
import io.github.jan.discordkm.api.entities.interactions.ComponentDsl
import io.github.jan.discordkm.api.entities.interactions.components.RowLayoutBuilder
import io.github.jan.discordkm.api.events.ModalSubmitEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ModalBuilder(@Transient override val client: DiscordClient? = null, @SerialName("custom_id") var customId: String = "", var title: String = "") : RowLayoutBuilder<ModalLayout>(client) {

    @ComponentDsl
    fun onSubmit(callback: suspend ModalSubmitEvent.() -> Unit) {
        if(client is WSDiscordClient) {
            client.on(predicate = { (it.modalId == customId) }, callback)
        }
    }

}