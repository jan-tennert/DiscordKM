package io.github.jan.discordkm.internal.events

import io.github.jan.discordkm.api.entities.Snowflake
import io.github.jan.discordkm.api.entities.clients.Client
import io.github.jan.discordkm.api.entities.guild.Sticker
import io.github.jan.discordkm.api.events.GuildStickersUpdateEvent
import io.github.jan.discordkm.internal.utils.getOrThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray

class GuildStickersUpdateEventHandler(val client: Client) : InternalEventHandler<GuildStickersUpdateEvent> {

    override fun handle(data: JsonObject): GuildStickersUpdateEvent {
        val stickers = data.getValue("stickers").jsonArray.map { Sticker(data, client) }
        val guild = client.guilds[data.getOrThrow<Snowflake>("guild_id")]!!
        return GuildStickersUpdateEvent(guild, stickers)
    }

}