/**
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.serialization

import com.soywiz.korio.net.ws.WebSocketClient
import com.soywiz.korio.util.OS
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class Payload(
    @SerialName("op")
    val opCode: Int,
    @SerialName("d")
    @Contextual
    val eventData: JsonObject? = null,
    @SerialName("s")
    val sequenceNumber: Int? = null,
    @SerialName("t")
    val eventName: String? = null
)

fun IdentifyPayload(token: String, intents: Long) = Payload(2, eventData = buildJsonObject {
    put("token", token)
    put("intents", intents)
    put("properties", buildJsonObject {
        put("\$os", OS.platformName)
        put("\$browser", "Discord.KM")
        put("\$device", "Discord.KM")
    })
})

suspend fun WebSocketClient.send(payload: Payload) = send(Json.encodeToString(payload))