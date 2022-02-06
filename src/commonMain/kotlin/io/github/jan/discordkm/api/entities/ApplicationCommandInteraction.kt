package io.github.jan.discordkm.api.entities

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.interactions.ModalInteraction
import io.github.jan.discordkm.api.entities.interactions.StandardInteraction
import kotlinx.serialization.json.JsonObject

class ApplicationCommandInteraction(client: Client, data: JsonObject) : StandardInteraction(client, data), ModalInteraction