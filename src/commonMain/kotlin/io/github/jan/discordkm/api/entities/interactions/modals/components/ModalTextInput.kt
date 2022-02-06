package io.github.jan.discordkm.api.entities.interactions.modals.components

import io.github.jan.discordkm.api.entities.interactions.components.ComponentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ModalTextInput(val value: String, @SerialName("custom_id") val customId: String) : ModalComponent {

    override val type: ComponentType = ComponentType.TEXT_INPUT

}