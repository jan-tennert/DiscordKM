package io.github.jan.discordkm.events.internal

import io.github.jan.discordkm.clients.DiscordClient
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.events.ReadyEvent
import io.github.jan.discordkm.utils.extractClientEntity
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

internal class ReadyEventHandler(val client: DiscordClient) : InternalEventHandler<ReadyEvent> {
    override fun handle(data: JsonObject): ReadyEvent {
        client.gateway.sessionId = data.getValue("session_id").jsonPrimitive.content
        val guilds = mutableListOf<Guild.Unavailable>()
        data.getValue("guilds").jsonArray.map { Guild.Unavailable(it.jsonObject.getValue("id").jsonPrimitive.long) }.forEach { guilds += it }
        client.selfUser = data.getValue("user").jsonObject.extractClientEntity(client)
        //shards
        //application
        return ReadyEvent(guilds, client)
    }
}