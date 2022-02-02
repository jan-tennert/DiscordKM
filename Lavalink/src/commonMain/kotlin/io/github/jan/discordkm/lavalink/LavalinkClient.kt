package io.github.jan.discordkm.lavalink

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.internal.utils.putJsonObject
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.cio.websocket.send
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class LavalinkClient(client: DiscordWebSocketClient) {

    val nodes = NodeList(client)

}

suspend fun DefaultClientWebSocketSession.send(opCode: String, guildId: Snowflake, builder: JsonObjectBuilder.() -> Unit = {}) = send(buildJsonObject {
    putJsonObject(buildJsonObject(builder))
    put("guildId", guildId.string)
    put("op", opCode)
}.toString())
