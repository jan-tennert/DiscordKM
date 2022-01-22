package io.github.jan.discordkm.api.entities.interactions.modals.components

import io.github.jan.discordkm.api.entities.interactions.components.ComponentType
import kotlinx.serialization.Required

sealed interface ModalComponent {

    @Required
    val type: ComponentType

}