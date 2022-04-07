package io.github.jan.discordkm.api.entities.interactions

import io.github.jan.discordkm.api.entities.clients.DiscordClient
import kotlinx.serialization.json.JsonObject

class ApplicationCommandInteraction(client: DiscordClient, data: JsonObject) : StandardInteraction(client, data), ModalInteraction