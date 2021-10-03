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
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.Sticker
import io.github.jan.discordkm.api.events.GuildStickersUpdateEvent
import io.github.jan.discordkm.internal.Cache
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray

class GuildStickersUpdateEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<GuildStickersUpdateEvent> {

    override fun handle(data: JsonObject): GuildStickersUpdateEvent {
        val stickers = data.getValue("stickers").jsonArray.map { Sticker(data, client) }
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        if(Cache.STICKERS in client.enabledCache) {
            val cache = (guild as GuildData).stickerCache
            cache.internalMap.clear()
            stickers.forEach { cache[it.id] = it }
        }
        return GuildStickersUpdateEvent(guild, stickers)
    }

}