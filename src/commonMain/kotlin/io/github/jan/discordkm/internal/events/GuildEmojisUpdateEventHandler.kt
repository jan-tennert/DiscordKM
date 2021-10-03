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
import io.github.jan.discordkm.api.entities.guild.Emoji
import io.github.jan.discordkm.api.events.GuildEmojisUpdateEvent
import io.github.jan.discordkm.internal.Cache
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class GuildEmojisUpdateEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<GuildEmojisUpdateEvent> {

    override fun handle(data: JsonObject): GuildEmojisUpdateEvent {
        val emotes = data.getValue("emojis").jsonArray.map { Emoji.Emote(it.jsonObject, client) }
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        if(Cache.EMOJIS in client.enabledCache) {
            val cache = (guild as GuildData).emojiCache
            cache.internalMap.clear()
            emotes.forEach { cache[it.id] = it }
        }
        return GuildEmojisUpdateEvent(guild, emotes)
    }

}