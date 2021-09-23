package io.github.jan.discordkm.events

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.interactions.components.ComponentInteraction
import io.github.jan.discordkm.entities.interactions.components.SelectOption

class ButtonClickEvent(override val client: Client, override val interaction: ComponentInteraction, val buttonId: String) : InteractionCreateEvent

class SelectionMenuEvent(override val client: Client, override val interaction: ComponentInteraction, val selectedOptions: List<SelectOption>, val selectionMenuId: String) : InteractionCreateEvent