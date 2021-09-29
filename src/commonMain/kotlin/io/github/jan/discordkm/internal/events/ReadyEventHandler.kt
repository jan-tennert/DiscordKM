/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.events.ReadyEvent
import io.github.jan.discordkm.internal.utils.extractClientEntity
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

internal class ReadyEventHandler(val client: DiscordWebSocketClient) : InternalEventHandler<ReadyEvent> {
    override fun handle(data: JsonObject): ReadyEvent {
        client.gateway.sessionId = data.getValue("session_id").jsonPrimitive.content
        val guilds = mutableListOf<Guild.Unavailable>()
        data.getValue("guilds").jsonArray.map { Guild.Unavailable(it.jsonObject.getValue("id").jsonPrimitive.long) }.forEach { guilds += it }
        client.selfUser = data.getValue("user").jsonObject.extractClientEntity<User>(client)
        //shards
        //application
        return ReadyEvent(guilds, client)
    }
}