package io.github.jan.discordkm.events.internal

import io.github.jan.discordkm.clients.Client
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.events.GuildUpdateEvent
import io.github.jan.discordkm.utils.extractClientEntity
import kotlinx.serialization.json.JsonObject

class GuildUpdateEventHandler(val client: Client) : InternalEventHandler<GuildUpdateEvent> {

    override fun handle(data: JsonObject): GuildUpdateEvent {
        val guild = data.extractClientEntity<Guild>(client)
        client.guildCache[guild.id] = guild
        return GuildUpdateEvent(client, guild)
    }

}