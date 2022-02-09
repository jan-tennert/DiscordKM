package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.clients.Client
import kotlinx.serialization.json.JsonObject

class ApplicationCommandInteraction(client: Client, data: JsonObject) : StandardInteraction(client, data), ModalInteraction