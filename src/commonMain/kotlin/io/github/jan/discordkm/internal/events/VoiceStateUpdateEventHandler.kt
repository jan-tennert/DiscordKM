/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.events.VoiceStateUpdateEvent
import io.github.jan.discordkm.internal.Cache
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.entities.guilds.VoiceStateData
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class VoiceStateUpdateEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<VoiceStateUpdateEvent> {

    override fun handle(data: JsonObject): VoiceStateUpdateEvent {
        val voiceState = VoiceStateData(client, data)
        val guildId = data.getOrThrow<Snowflake>("guild_id")
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")] ?: throw IllegalStateException("Guild with id $guildId couldn't be found on event GuildMemberUpdateEvent. The guilds probably aren't done initialising.")
        val oldVoiceState = guild.voiceStates.firstOrNull { it.userId == voiceState.userId }
        guild.let {
            if(Cache.VOICE_STATES in client.enabledCache) {
                (it as GuildData).voiceStateCache.remove(voiceState.userId)
                (it as GuildData).voiceStateCache[voiceState.userId] =  voiceState
            }
        }
        return VoiceStateUpdateEvent(client, voiceState, oldVoiceState)
    }

}