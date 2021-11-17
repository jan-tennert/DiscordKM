/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import com.soywiz.klogger.Logger
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.events.GuildDeleteEvent
import io.github.jan.discordkm.internal.utils.getOrThrow
import io.github.jan.discordkm.internal.utils.snowflake
import kotlinx.serialization.json.JsonObject

class GuildDeleteEventHandler(val client: Client, private val LOGGER: Logger) :
    InternalEventHandler<GuildDeleteEvent> {

    override suspend fun handle(data: JsonObject): GuildDeleteEvent {
        val id = data["id"]!!.snowflake
        if(data.contains("unavailable")) {
            LOGGER.warn { "The guild \"$id\" is unavailable due to an outage" }
        }
        client.cacheManager.guildCache.remove(id)
        return GuildDeleteEvent(client, id)
    }

}