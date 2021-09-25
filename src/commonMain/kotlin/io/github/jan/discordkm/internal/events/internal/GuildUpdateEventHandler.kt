package io.github.jan.discordkm.internal.events.internal

import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Guild
import io.github.jan.discordkm.api.events.GuildUpdateEvent
import io.github.jan.discordkm.internal.utils.extractClientEntity
import kotlinx.serialization.json.JsonObject

class GuildUpdateEventHandler(val client: Client) : InternalEventHandler<GuildUpdateEvent> {

    override fun handle(data: JsonObject): GuildUpdateEvent {
        val guild = data.extractClientEntity<Guild>(client)
        client.guildCache[guild.id] = guild
        return GuildUpdateEvent(client, guild)
    }

}