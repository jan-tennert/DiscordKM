package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.interactions.ComponentInteraction
import io.github.jan.discordkm.api.entities.interactions.Interaction
import io.github.jan.discordkm.api.entities.interactions.components.ComponentType
import io.github.jan.discordkm.api.entities.interactions.components.SelectOption

interface ComponentEvent : InteractionCreateEvent {
    override val interaction: ComponentInteraction
    val componentId: String
    val componentType: ComponentType
}

class ButtonClickEvent(
    override val client: Client,
    override val interaction: ComponentInteraction,
    override val componentId: String,
) : ComponentEvent {

    override val componentType: ComponentType = ComponentType.BUTTON

}

class SelectionMenuEvent(
    override val client: Client,
    override val interaction: ComponentInteraction,
    val selectedOptions: List<SelectOption>,
    override val componentId: String,
) : ComponentEvent {

    override val componentType: ComponentType = ComponentType.SELECTION_MENU

}