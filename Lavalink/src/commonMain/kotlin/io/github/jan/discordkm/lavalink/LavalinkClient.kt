package io.github.jan.discordkm.lavalink

import com.soywiz.korio.net.ws.WebSocketClient
import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.DiscordWebSocketClient
import io.github.jan.discordkm.internal.utils.putJsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class LavalinkClient(client: DiscordWebSocketClient) {

    val nodes = NodeList(client)

}

suspend fun WebSocketClient.send(opCode: String, guildId: Snowflake, builder: JsonObjectBuilder.() -> Unit = {}) = send(buildJsonObject {
    putJsonObject(buildJsonObject(builder))
    put("guildId", guildId.string)
    put("op", opCode)
}.toString())
