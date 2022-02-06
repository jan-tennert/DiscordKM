package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.interactions.StandardInteraction
import io.github.jan.discordkm.api.entities.interactions.modals.components.ModalRow

class ModalSubmitEvent(override val client: Client, val modalId: String, val rows: List<ModalRow>, override val interaction: StandardInteraction) : InteractionCreateEvent