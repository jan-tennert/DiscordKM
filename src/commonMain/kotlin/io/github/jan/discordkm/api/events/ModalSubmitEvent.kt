package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.containers.ComponentContainer
import io.github.jan.discordkm.api.entities.interactions.StandardInteraction
import io.github.jan.discordkm.api.entities.interactions.modals.components.ModalComponent

class ModalSubmitEvent(override val client: Client, val modalId: String, val components: ComponentContainer<ModalComponent>, override val interaction: StandardInteraction) : InteractionCreateEvent