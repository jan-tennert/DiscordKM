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
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.events.GuildUpdateEvent
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class GuildUpdateEventHandler(val client: Client) : InternalEventHandler<GuildUpdateEvent> {

    override fun handle(data: JsonObject): GuildUpdateEvent {
        val guildId = data.getOrThrow<Snowflake>("guild_id")
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")] ?: throw IllegalStateException("Guild with id $guildId couldn't be found on event GuildMemberUpdateEvent. The guilds probably aren't done initialising.")
        (guild as GuildData).update(data)
        return GuildUpdateEvent(client, guild)
    }

}