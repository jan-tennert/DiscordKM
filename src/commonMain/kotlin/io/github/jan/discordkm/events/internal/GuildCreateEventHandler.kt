package io.github.jan.discordkm.events.internal

import io.github.jan.discordkm.Client
import io.github.jan.discordkm.entities.guild.Guild
import io.github.jan.discordkm.events.GuildCreateEvent
import io.github.jan.discordkm.utils.extractClientEntity
import kotlinx.serialization.json.JsonObject

internal class GuildCreateEventHandler(val client: Client) : InternalEventHandler<GuildCreateEvent> {

    override fun handle(data: JsonObject): GuildCreateEvent {
        val guild = data.extractClientEntity<Guild>(client)
        client.guildCache[guild.id] = guild
        return GuildCreateEvent(guild, client)
    }

}