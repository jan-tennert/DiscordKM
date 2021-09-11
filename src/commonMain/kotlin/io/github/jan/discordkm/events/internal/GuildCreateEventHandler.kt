/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.events.internal

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.events.GuildCreateEvent
import io.github.jan.discordkm.utils.extractClientEntity
import kotlinx.serialization.json.JsonObject

internal class GuildCreateEventHandler(val client: Client) : InternalEventHandler<GuildCreateEvent> {

    override fun handle(data: JsonObject): GuildCreateEvent {
        val guild = data.extractClientEntity<Guild>(client)
        client.guildCache[guild.id] = guild
        return GuildCreateEvent(guild, client)
    }

}