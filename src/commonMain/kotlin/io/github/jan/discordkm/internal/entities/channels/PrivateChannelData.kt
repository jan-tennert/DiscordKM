package io.github.jan.discordkm.internal.entities.channels

import io.github.jan.discordkm.api.entities.clients.Client
import kotlinx.serialization.json.JsonObject

class PrivateChannelData(client: Client, data: JsonObject) : MessageChannelData(client, data), PrivateChannel