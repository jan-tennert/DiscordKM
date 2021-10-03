package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.internal.serialization.Payload

class RawEvent(override val client: Client, val payload: Payload) : Event