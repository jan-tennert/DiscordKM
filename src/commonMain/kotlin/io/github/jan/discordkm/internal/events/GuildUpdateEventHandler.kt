package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.events.GuildUpdateEvent
import io.github.jan.discordkm.internal.entities.guilds.GuildData
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject

class GuildUpdateEventHandler(val client: Client) : InternalEventHandler<GuildUpdateEvent> {

    override fun handle(data: JsonObject): GuildUpdateEvent {
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        (guild as GuildData).update(data)
        return GuildUpdateEvent(client, guild)
    }

}