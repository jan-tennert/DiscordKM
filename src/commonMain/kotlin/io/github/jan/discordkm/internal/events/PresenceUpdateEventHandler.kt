/*
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
import io.github.jan.discordkm.api.events.PresenceUpdateEvent
import io.github.jan.discordkm.internal.serialization.serializers.GuildSerializer
import io.github.jan.discordkm.internal.utils.snowflake
import kotlinx.serialization.json.JsonObject

internal class PresenceUpdateEventHandler(val client: DiscordClient) : InternalEventHandler<PresenceUpdateEvent> {

    override suspend fun handle(data: JsonObject): PresenceUpdateEvent {
        val guild = Guild(data["guild_id"]!!.snowflake, client)
        val presence = GuildSerializer.deserializeGuildPresence(data, client)
        val oldPresence = guild.cache?.presences?.get(presence.user.id)
        guild.cache?.cacheManager?.presences?.set(presence.user.id, presence)
        return PresenceUpdateEvent(presence, oldPresence)
    }

}