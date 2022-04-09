/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.lavalink

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.WSDiscordClient
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.cio.websocket.send
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface LavalinkClient {

    val client: WSDiscordClient
    val nodes: NodeContainer

}

internal class LavalinkClientImpl(override val client: WSDiscordClient): LavalinkClient {

    override val nodes: NodeContainer = NodeContainerImpl(client)

}

suspend fun DefaultClientWebSocketSession.send(opCode: String, guildId: Snowflake, builder: JsonObjectBuilder.() -> Unit = {}) = send(buildJsonObject {
    builder()
    put("guildId", guildId.string)
    put("op", opCode)
}.toString().also(::println))

fun WSDiscordClient.createLavalinkClient(): LavalinkClient = LavalinkClientImpl(this)
