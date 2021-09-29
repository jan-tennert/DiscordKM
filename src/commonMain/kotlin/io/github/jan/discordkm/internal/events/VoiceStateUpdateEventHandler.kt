package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.events.VoiceStateUpdateEvent
import io.github.jan.discordkm.internal.Cache
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.entities.guilds.MemberData
import io.github.jan.discordkm.internal.entities.guilds.VoiceStateData
import kotlinx.serialization.json.JsonObject

class VoiceStateUpdateEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<VoiceStateUpdateEvent> {

    override fun handle(data: JsonObject): VoiceStateUpdateEvent {
        val voiceState = VoiceStateData(client, data)
        val guild = client.guilds[voiceState.guildId ?: Snowflake.empty()]!!
        val oldVoiceState = guild.voiceStates.firstOrNull { it.userId == voiceState.userId }
        guild.let {
            if(Cache.VOICE_STATES in client.enabledCache) {
                it.members[voiceState.userId]?.let { member ->
                    (member as MemberData).voiceState = voiceState
                }
                (it as GuildData).voiceStates.removeAll { state -> state.userId == voiceState.userId }
                (it as GuildData).voiceStates += voiceState
            }
        }
        return VoiceStateUpdateEvent(client, voiceState, oldVoiceState)
    }

}