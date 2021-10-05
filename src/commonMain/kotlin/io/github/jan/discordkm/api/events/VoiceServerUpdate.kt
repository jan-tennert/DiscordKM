package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import kotlinx.serialization.json.JsonObject

class VoiceServerUpdate(override val client: Client, data: JsonObject) : Event