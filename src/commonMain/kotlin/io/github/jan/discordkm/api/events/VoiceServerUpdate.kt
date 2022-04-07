package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.DiscordClient
import kotlinx.serialization.json.JsonObject

class VoiceServerUpdate(override val client: DiscordClient, val data: JsonObject) : Event