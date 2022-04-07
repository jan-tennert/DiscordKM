package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.containers.ComponentContainer
import io.github.jan.discordkm.api.entities.interactions.StandardInteraction
import io.github.jan.discordkm.api.entities.interactions.modals.components.ModalComponent

class ModalSubmitEvent(override val client: DiscordClient, val modalId: String, val components: ComponentContainer<ModalComponent>, override val interaction: StandardInteraction) : InteractionCreateEvent