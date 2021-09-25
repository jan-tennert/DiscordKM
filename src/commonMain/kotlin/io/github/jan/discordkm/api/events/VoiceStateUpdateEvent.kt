package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.VoiceState

class VoiceStateUpdateEvent(override val client: Client, val voiceState: VoiceState) : Event