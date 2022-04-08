/*
 * DiscordKM is a kotlin multiplatform Discord API Wrapper
 * Copyright (C) 2021 Jan Tennert
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

 */
package io.github.jan.discordkm.internal.serialization

import com.soywiz.korio.util.OS
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.activity.Presence
import io.github.jan.discordkm.api.entities.activity.PresenceModifier
import io.github.jan.discordkm.api.entities.activity.PresenceStatus
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.cio.websocket.send
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

@Serializable
data class Payload(
    @SerialName("op")
    val opCode: Int,
    @SerialName("d")
    @Contextual
    val eventData: JsonElement? = null,
    @SerialName("s")
    val sequenceNumber: Int? = null,
    @SerialName("t")
    val eventName: String? = null
)

fun IdentifyPayload(token: String, intents: Long, status: PresenceStatus, activity: Presence?, shardId: Int = 0, totalShards: Int = -1) = Payload(2, eventData = buildJsonObject {
    put("token", token)
    put("intents", intents)
    if(totalShards != -1) {
        putJsonArray("shard") {
            add(shardId)
            add(totalShards)
        }
    }
    put("properties", buildJsonObject {
        put("\$os", OS.platformName)
        put("\$browser", "Discord.KM")
        put("\$device", "Discord.KM")
    })
    putJsonObject("presence") {
        put("status", status.value)

        activity?.let {
            putJsonArray("activities") {
                add(it.build())
            }
        }
    }
})

fun UpdateVoiceStatePayload(guildId: Snowflake, channelId: Snowflake?, selfMute: Boolean, selfDeaf: Boolean) = Payload(4, eventData = buildJsonObject {
    put("guild_id", guildId.string)
    put("channel_id", channelId?.string)
    put("self_mute", selfMute)
    put("self_deaf", selfDeaf)
})

fun RequestGuildMemberPayload(guildId: Snowflake, query: String?, limit: Int = 0, receivePresences: Boolean = false, users: Collection<Snowflake>) = Payload(8, eventData = buildJsonObject {
    put("guild_id", guildId.string)
    put("query", query ?: "")
    put("limit", limit)
    put("presence", receivePresences)
    putJsonArray("user_ids") {
        users.forEach {
            add(it.string)
        }
    }
})

fun UpdatePresencePayload(modifier: PresenceModifier) = Payload(3, eventData = modifier.build())

suspend fun DefaultClientWebSocketSession.send(payload: Payload) = send(Json.encodeToString(payload))