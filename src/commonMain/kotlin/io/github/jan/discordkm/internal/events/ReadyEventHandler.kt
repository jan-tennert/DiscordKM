/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.events

import com.soywiz.klogger.Logger
import io.github.jan.discordkm.api.entities.User
import io.github.jan.discordkm.api.entities.clients.DiscordClient
import io.github.jan.discordkm.api.entities.clients.WSDiscordClientImpl
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.events.ReadyEvent
import io.github.jan.discordkm.internal.utils.log
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

internal class ReadyEventHandler(val client: DiscordClient) : InternalEventHandler<ReadyEvent> {
    override suspend fun handle(data: JsonObject): ReadyEvent {
        if(client !is WSDiscordClientImpl) throw IllegalStateException("Client is not a DiscordWebSocketClient")
        val shardId = data["shard"]?.jsonArray?.get(1)?.jsonPrimitive?.intOrNull
        val selfUser = User(data["user"]!!.jsonObject, client)
        if(shardId != null) {
            client.shardConnections[shardId]?.let {
                it.mutex.withLock {
                    it.sessionId = data.getValue("session_id").jsonPrimitive.content
                }
            }
        } else {
            client.shardConnections[0]?.mutex?.withLock {
                client.shardConnections[0]?.sessionId = data.getValue("session_id").jsonPrimitive.content
            }
        }
        val guilds = mutableListOf<Guild.Unavailable>()
        data.getValue("guilds").jsonArray.map { Guild.Unavailable(it.jsonObject.getValue("id").jsonPrimitive.long) }.forEach { guilds += it }
        client.updateSelfUser(selfUser)
        val LOGGER = client.shardConnections[shardId ?: 0]?.LOGGER
        LOGGER?.log(true, Logger.Level.INFO) { "Finished authentication!" }
        LOGGER?.log(true, Logger.Level.INFO) { "Logged in as ${selfUser.name}#${selfUser.discriminator}" }
        return ReadyEvent(guilds, client, shardId)
    }
}