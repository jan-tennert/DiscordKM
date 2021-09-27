package io.github.jan.discordkm.api.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.VoiceState

/**
 * Sent when a member joins, leaves or moves to a voice channel
 */
class VoiceStateUpdateEvent(override val client: Client, val voiceState: VoiceState) : Event