/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.cacheManager
import io.github.jan.discordkm.api.events.GuildUpdateEvent
import io.github.jan.discordkm.internal.serialization.serializers.GuildSerializer
import kotlinx.serialization.json.JsonObject

class GuildUpdateEventHandler(val client: Client) : InternalEventHandler<GuildUpdateEvent> {

    override suspend fun handle(data: JsonObject): GuildUpdateEvent {
        val guild = GuildSerializer.deserialize(data, client)
        val oldGuild = client.guilds[guild.id]
        oldGuild?.cacheManager?.fillCache(guild.cacheManager)
        client.cacheManager.guildCache[guild.id] = guild
        return GuildUpdateEvent(client, guild, oldGuild)
    }

}