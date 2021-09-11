package com.github.jan.discordkm.serialization

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