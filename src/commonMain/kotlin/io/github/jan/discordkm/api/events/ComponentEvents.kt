package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.interactions.ComponentInteraction
import io.github.jan.discordkm.api.entities.interactions.components.ComponentType
import io.github.jan.discordkm.api.entities.interactions.components.SelectOption

interface ComponentEvent : InteractionCreateEvent {
    override val interaction: ComponentInteraction
    val componentId: String
    val componentType: ComponentType
}

/**
 * Sent when someone clicks on a button, which the bot created
 */
class ButtonClickEvent(
    override val client: Client,
    override val interaction: ComponentInteraction,
    override val componentId: String,
) : ComponentEvent {

    override val componentType: ComponentType = ComponentType.BUTTON

}

/**
 * Sent when someone interacts with a selection menu, which the bot created
 */
class SelectionMenuEvent(
    override val client: Client,
    override val interaction: ComponentInteraction,
    val selectedOptions: List<SelectOption>,
    override val componentId: String,
) : ComponentEvent {

    override val componentType: ComponentType = ComponentType.SELECTION_MENU

}