/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.events.VoiceStateUpdateEvent
import io.github.jan.discordkm.internal.serialization.serializers.VoiceStateSerializer
import io.github.jan.discordkm.internal.utils.snowflake
import kotlinx.serialization.json.JsonObject

internal class VoiceStateUpdateEventHandler(val client: DiscordClient) : InternalEventHandler<VoiceStateUpdateEvent> {

    override suspend fun handle(data: JsonObject): VoiceStateUpdateEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val voiceState = VoiceStateSerializer.deserialize(data, guild)
        val oldVoiceState = guild.cache?.cacheManager?.voiceStates?.get(voiceState.user.id)
        guild.cache?.cacheManager?.voiceStates?.set(voiceState.user.id, voiceState)
        return VoiceStateUpdateEvent(client, voiceState, oldVoiceState)
    }

}